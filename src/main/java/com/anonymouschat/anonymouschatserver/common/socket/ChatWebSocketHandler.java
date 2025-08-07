package com.anonymouschat.anonymouschatserver.common.socket;

import com.anonymouschat.anonymouschatserver.application.event.ChatMessageSaveEvent;
import com.anonymouschat.anonymouschatserver.application.event.ChatMessageSentEvent;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.common.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.common.socket.dto.ChatOutboundMessage;
import com.anonymouschat.anonymouschatserver.common.socket.dto.MessageType;
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
			// todo: 실제 서비스 화면을 보고 삭제할지 말지 결정
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

			switch (inbound.type()) {
				case ENTER -> handleEnter(senderId, inbound.roomId());
				case LEAVE -> handleLeave(senderId, inbound.roomId());
				case MESSAGE -> handleMessage(session, senderId, inbound);
				case HEARTBEAT -> log.debug("[HEARTBEAT] senderId={} pong", senderId); // 클라이언트 단 pong 수신 처리만 하는 경우
				default -> throw new IllegalArgumentException("지원하지 않는 메시지 타입입니다.");
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

			// 세션 제거
			sessionManager.unregisterSession(userId);

			// 모든 room 에서 퇴장 처리
			// 또는, 마지막으로 참여한 roomId를 세션에 기록해두었다면 그 하나만 처리
			Set<Long> roomIds = sessionManager.getJoinedRoomsByUser(userId);
			for (Long roomId : roomIds) {
				sessionManager.leaveRoom(roomId, userId);
				log.info("[AUTO LEAVE] userId={} roomId={} (연결 종료)", userId, roomId);
			}

			log.info("[Disconnected] userId={} status={}", userId, status);

		} catch (Exception e) {
			log.warn("연결 종료 처리 중 예외: {}", e.getMessage(), e);
		}
	}


	@Override
	public void handleTransportError(@NonNull WebSocketSession session, Throwable exception) {
		log.error("WebSocket 전송 오류 발생: {}", exception.getMessage());
		closeSession(session, CloseStatus.SERVER_ERROR);
	}

	private void handleEnter(Long userId, Long roomId) {
		sessionManager.joinRoom(roomId, userId);
		log.info("[ENTER] userId={} roomId={}", userId, roomId);
	}

	private void handleLeave(Long userId, Long roomId) {
		sessionManager.leaveRoom(roomId, userId);
		log.info("[LEAVE] userId={} roomId={}", userId, roomId);
	}

	private void handleMessage(WebSocketSession session, Long senderId, ChatInboundMessage inbound) {
		try {
			Long roomId = inbound.roomId();
			String content = inbound.content();
			String nickname = resolveNicknameOrClose(senderId, session);

			ChatOutboundMessage outbound = ChatOutboundMessage.builder()
					                               .roomId(roomId)
					                               .type(MessageType.MESSAGE)
					                               .senderId(senderId)
					                               .senderNickname(nickname)
					                               .content(content)
					                               .timestamp(Instant.now())
					                               .build();

			// 메시지 상태 변경 이벤트
			applicationEventPublisher.publishEvent(
					ChatMessageSentEvent.builder().roomId(roomId).build());

			// 메시지 저장 이벤트
			applicationEventPublisher.publishEvent(
					ChatMessageSaveEvent.builder()
							.chatRoomId(roomId)
							.senderId(senderId)
							.content(content)
							.build());

			// 채팅방 참여자에게 메시지 브로드캐스트
			broadcastToRoom(roomId, outbound);

		} catch (Exception e) {
			log.error("채팅 메시지 처리 중 오류 발생: {}", e.getMessage(), e);
			closeSession(session, CloseStatus.SERVER_ERROR);
		}
	}

	private void broadcastToRoom(Long roomId, ChatOutboundMessage message) {
		try {
			String payload = objectMapper.writeValueAsString(message);
			for (Long participantId : sessionManager.getParticipants(roomId)) {
				WebSocketSession targetSession = sessionManager.getSession(participantId);
				if (targetSession != null && targetSession.isOpen()) {
					targetSession.sendMessage(new TextMessage(payload));
				}
			}
		} catch (Exception e) {
			log.error("브로드캐스트 실패: {}", e.getMessage(), e);
		}
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

	private String resolveNicknameOrClose(Long userId, WebSocketSession session) {
		try {
			return userService.findUser(userId).getNickname();
		} catch (Exception e) {
			log.warn("닉네임 조회 실패: userId={}", userId, e);
			closeSession(session, CloseStatus.SERVER_ERROR);
			throw new RuntimeException("닉네임 조회 실패", e);
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
}
