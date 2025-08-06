package com.anonymouschat.anonymouschatserver.common.socket;

import com.anonymouschat.anonymouschatserver.application.event.ChatMessageSaveEvent;
import com.anonymouschat.anonymouschatserver.application.event.ChatMessageSentEvent;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.common.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.common.socket.dto.ChatOutboundMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.*;

import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Set;

/**
 * 실시간 채팅 처리를 위한 WebSocket 핸들러.
 * 인증된 사용자만 메시지를 송수신할 수 있으며, 채팅방 내 참여자에게 메시지를 브로드캐스트합니다.
 */
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {
	private final ApplicationEventPublisher applicationEventPublisher;
	private final ChatSessionManager sessionManager;
	private final ObjectMapper objectMapper;
	private final UserService userService;

	private static final String ATTR_PRINCIPAL = "principal";

	@Override
	public void afterConnectionEstablished(@NonNull WebSocketSession session) {
		try {
			CustomPrincipal principal = extractPrincipal(session);
			Long userId = principal.userId();

			// 기존 세션 제거 (중복 방지)
			WebSocketSession existing = sessionManager.getSession(userId);
			if (existing != null && existing.isOpen()) {
				closeSession(existing, CloseStatus.NORMAL);
			}

			sessionManager.registerSession(userId, session);
			log.info("[Connected] userId={} sessionId={}", userId, session.getId());

		} catch (Exception e) {
			log.warn("WebSocket 연결 실패: {}", e.getMessage());
			closeSession(session, CloseStatus.NOT_ACCEPTABLE);
		}
	}

	@Override
	protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage textMessage) {
		try {
			CustomPrincipal principal = extractPrincipal(session);
			Long senderId = principal.userId();

			ChatInboundMessage inbound = objectMapper.readValue(textMessage.getPayload(), ChatInboundMessage.class);

			// 세션 관리에 참여자 등록 (처음 입장 시)
			sessionManager.joinRoom(inbound.roomId(), senderId);

			String nickname = resolveNicknameOrClose(senderId, session);

			ChatOutboundMessage outbound = ChatOutboundMessage.builder()
					                            .roomId(inbound.roomId())
					                            .type(inbound.type())
					                            .senderId(senderId)
					                            .senderNickname(nickname)
					                            .content(inbound.content())
					                            .timestamp(Instant.now())
					                            .build();

			//채팅방 상태 변경 Inactive -> Active 이벤트 발행
			applicationEventPublisher.publishEvent(
					ChatMessageSentEvent.builder()
							.roomId(inbound.roomId())
							.build());


			//메시지 저장 이벤트 발행
			applicationEventPublisher.publishEvent(
					ChatMessageSaveEvent.builder()
							.chatRoomId(inbound.roomId())
							.senderId(senderId)
							.content(inbound.content()).build());

			// 채팅방 참여자에게 메시지 전송
			Set<Long> participants = sessionManager.getParticipants(inbound.roomId());
			for (Long userId : participants) {
				WebSocketSession targetSession = sessionManager.getSession(userId);
				if (targetSession != null && targetSession.isOpen()) {
					String payload = objectMapper.writeValueAsString(outbound);
					targetSession.sendMessage(new TextMessage(payload));
				}
			}
		} catch (Exception e) {
			log.error("WebSocket 메시지 처리 중 예외: {}", e.getMessage());
			closeSession(session, CloseStatus.SERVER_ERROR);
		}
	}

	@Override
	public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
		try {
			CustomPrincipal principal = extractPrincipal(session);
			Long userId = principal.userId();

			sessionManager.unregisterSession(userId);
			log.info("[Disconnected] userId={} status={}", userId, status);

		} catch (Exception e) {
			log.warn("연결 종료 처리 중 예외: {}", e.getMessage());
		}
	}

	@Override
	public void handleTransportError(@NonNull WebSocketSession session, Throwable exception) {
		log.error("WebSocket 전송 오류 발생: {}", exception.getMessage());
		closeSession(session, CloseStatus.SERVER_ERROR);
	}

	private CustomPrincipal extractPrincipal(WebSocketSession session) {
		Object attr = session.getAttributes().get(ATTR_PRINCIPAL);
		if (attr instanceof CustomPrincipal principal) {
			if (!principal.isAuthenticatedUser()) {
				throw new IllegalStateException("인증되지 않은 사용자는 메시지를 보낼 수 없습니다.");
			}
			return principal;
		}
		throw new IllegalStateException("WebSocket 세션에 인증 정보가 없습니다.");
	}

	private void closeSession(WebSocketSession session, CloseStatus status) {
		try {
			session.close(status);
		} catch (Exception e) {
			log.error("세션 종료 실패: {}", e.getMessage());
		}
	}

	/**
	 * 현재 연결된 세션 목록을 순회하며 Ping 메시지를 보낸다.
	 * 응답 없거나 예외 발생 시 세션 종료 처리할 수 있음.
	 */
	@Scheduled(fixedRate = 30_000)
	public void sendPingToAllSessions() {
		sessionManager.getAllSessions().forEach((userId, session) -> {
			if (session.isOpen()) {
				try {
					ByteBuffer payload = ByteBuffer.wrap("ping".getBytes());
					session.sendMessage(new PingMessage(payload));
				} catch (IOException e) {
					log.warn("Ping 실패: userId={} - 세션 종료 처리", userId);
					try {
						session.close();
					} catch (IOException ignored) {
					}
				}
			}
		});
	}

	private String resolveNicknameOrClose(Long userId, WebSocketSession session) {
		try {
			return userService.findUser(userId).getNickname();
		} catch (Exception e) {
			log.warn("닉네임 조회 실패: userId={}", userId, e);
			closeSession(session, CloseStatus.SERVER_ERROR);
			throw new RuntimeException("닉네임 조회 실패", e);
		}
	}
}
