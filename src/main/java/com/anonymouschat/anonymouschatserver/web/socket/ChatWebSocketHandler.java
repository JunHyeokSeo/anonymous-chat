package com.anonymouschat.anonymouschatserver.web.socket;

import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatInboundMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static com.anonymouschat.anonymouschatserver.web.socket.support.WebSocketUtil.extractPrincipal;

/**
 * 실시간 채팅 처리를 위한 WebSocket 핸들러.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {
	private final ChatMessageDispatcher dispatcher;
	private final ChatSessionManager sessionManager;
	private final ObjectMapper objectMapper;

	@Override
	public void afterConnectionEstablished(@NonNull WebSocketSession session) {
		try {
			CustomPrincipal principal = extractPrincipal(session);
			Long userId = principal.userId();

			// 중복 세션 교체 + lastActive 초기화까지 내부에서 처리
			sessionManager.registerOrReplaceSession(userId, session);
			log.info("[WS] Connected userId={} sessionId={}", userId, session.getId());
		} catch (Exception e) {
			log.warn("[WS] connection rejected: {}", e.getMessage());
			sessionManager.forceDisconnect(session, CloseStatus.NOT_ACCEPTABLE);
		}
	}


	@Override
	protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage textMessage) {
		try {
			ChatInboundMessage inbound = objectMapper.readValue(textMessage.getPayload(), ChatInboundMessage.class);
			dispatcher.dispatch(session, inbound);
		} catch (UnsupportedOperationException | IllegalArgumentException bad) {
			log.warn("[WS-BAD_DATA] sessionId={} reason={}", session.getId(), bad.getMessage());
			sessionManager.forceDisconnect(session, CloseStatus.BAD_DATA);
		} catch (Exception e) {
			log.error("[WS-ERROR] sessionId={} error={}", session.getId(), e.getMessage(), e);
			sessionManager.forceDisconnect(session, CloseStatus.SERVER_ERROR);
		}
	}

	@Override
	public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
		try {
			CustomPrincipal principal = extractPrincipal(session);
			Long userId = principal.userId();
			sessionManager.forceDisconnect(userId, status);
			log.info("[Disconnected] userId={} status={}", userId, status);
		} catch (Exception e) {
			log.warn("연결 종료 처리 중 예외: {}", e.getMessage(), e);
		}
	}

	@Override
	public void handleTransportError(@NonNull WebSocketSession session, Throwable exception) {
		log.error("WebSocket 전송 오류 발생: {}", exception.getMessage());
		sessionManager.forceDisconnect(session, CloseStatus.SERVER_ERROR);
	}

	@Override
	public void handlePongMessage(@NonNull WebSocketSession session, @NonNull PongMessage message) {
		try {
			Long userId = extractPrincipal(session).userId();
			sessionManager.updateLastActiveAt(userId);
			log.debug("[WS] PONG userId={}", userId);
		} catch (Exception e) {
			log.warn("[WS] pong handle failed: {}", e.getMessage());
		}
	}


	/**
	 * 현재 연결된 세션 목록을 순회하며 Ping 메시지를 보낸다.
	 * 응답 없거나 예외 발생 시 세션 종료 처리할 수 있음.
	 */
	@Scheduled(fixedRate = 30_000)
	public void sendPingToAllSessions() {
		Instant now = Instant.now();

		sessionManager.getAllSessions().forEach((userId, session) -> {
			if (!session.isOpen()) {
				log.debug("이미 닫힌 세션: userId={}", userId);
				return;
			}

			// Ping 메시지 전송
			boolean pingSent = sendPing(session, userId);
			if (!pingSent) {
				sessionManager.forceDisconnect(userId, CloseStatus.SESSION_NOT_RELIABLE);
				return;
			}

			// 유휴 시간 체크 (무응답 클라이언트 정리)
			Instant lastActiveAt = sessionManager.getLastActiveAt(userId);
			if (lastActiveAt.plusSeconds(60).isBefore(now)) {
				log.warn("세션 유휴 상태 감지: userId={} lastActiveAt={}", userId, lastActiveAt);
				sessionManager.forceDisconnect(userId, CloseStatus.SESSION_NOT_RELIABLE);
			}
		});
	}

	/**
	 * Ping 메시지를 전송하고 성공 여부를 반환합니다.
	 */
	private boolean sendPing(WebSocketSession session, Long userId) {
		try {
			long timestamp = Instant.now().toEpochMilli();
			byte[] payload = Long.toString(timestamp).getBytes(StandardCharsets.UTF_8);
			session.sendMessage(new PingMessage(ByteBuffer.wrap(payload)));
			log.debug("Ping 전송 완료: userId={}", userId);
			return true;
		} catch (IOException e) {
			log.warn("Ping 전송 실패: userId={} reason={}", userId, e.getMessage());
			return false;
		}
	}
}
