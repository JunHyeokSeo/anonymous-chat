package com.anonymouschat.anonymouschatserver.presentation.socket;

import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.presentation.socket.support.WebSocketRateLimitGuard;
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

import static com.anonymouschat.anonymouschatserver.presentation.socket.support.WebSocketUtil.extractPrincipal;
import static com.anonymouschat.anonymouschatserver.presentation.socket.support.WebSocketUtil.extractUserId;

/**
 * 실시간 채팅 처리를 위한 WebSocket 핸들러.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {
	private final WebSocketRateLimitGuard rateLimitGuard;
	private final ChatMessageDispatcher dispatcher;
	private final ChatSessionManager sessionManager;
	private final ObjectMapper objectMapper;

	@Override
	public void afterConnectionEstablished(@NonNull WebSocketSession session) {
		try {
			CustomPrincipal principal = extractPrincipal(session);
			Long userId = principal.userId();
			sessionManager.registerOrReplaceSession(userId, session);
			log.info("{}connected userId={} sessionId={}", LogTag.WS_SYS, userId, session.getId());
		} catch (Exception e) {
			log.warn("{}connection rejected sessionId={} reason={}", LogTag.WS_ERR, session.getId(), e.getMessage());
			sessionManager.forceDisconnect(session, CloseStatus.NOT_ACCEPTABLE);
		}
	}

	@Override
	protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage textMessage) {
		try {
			Long userId = extractUserId(session);
			sessionManager.updateLastActiveAt(userId);
			ChatInboundMessage inbound = objectMapper.readValue(textMessage.getPayload(), ChatInboundMessage.class);

			// 레이트 리밋 컷
			if (!rateLimitGuard.allow(userId, inbound.type())) {
				log.warn("{}rate limited userId={} type={} sessionId={}", LogTag.WS_ERR, userId, inbound.type(), session.getId());
				return;
			}

			log.debug("{}dispatch start sessionId={} type={} roomId={}", LogTag.WS_SYS, session.getId(), inbound.type(), inbound.roomId());
			dispatcher.dispatch(session, inbound);
		} catch (UnsupportedOperationException | IllegalArgumentException bad) {
			log.warn("{}bad payload sessionId={} reason={}", LogTag.WS_ERR, session.getId(), bad.getMessage(), bad);
			sessionManager.forceDisconnect(session, CloseStatus.BAD_DATA);
		} catch (Exception e) {
			log.error("{}handleText error sessionId={} error={}", LogTag.WS_ERR, session.getId(), e.getMessage(), e);
			sessionManager.forceDisconnect(session, CloseStatus.SERVER_ERROR);
		}
	}

	@Override
	public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
		try {
			Long userId = extractUserId(session);
			log.info("{}disconnected userId={} sessionId={} status={}", LogTag.WS_SYS, userId, session.getId(), status);
			rateLimitGuard.clear(userId);
			sessionManager.forceDisconnect(userId, status);
		} catch (Exception e) {
			log.warn("{}afterClose error sessionId={} reason={}", LogTag.WS_ERR, session.getId(), e.getMessage());
		}
	}

	@Override
	public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) {
		log.error("{}transport error sessionId={} error={}", LogTag.WS_ERR, session.getId(), exception.getMessage(), exception);
		sessionManager.forceDisconnect(session, CloseStatus.SERVER_ERROR);
	}

	@Override
	public void handlePongMessage(@NonNull WebSocketSession session, @NonNull PongMessage message) {
		try {
			Long userId = extractUserId(session);
			sessionManager.updateLastActiveAt(userId);
			log.debug("{}userId={} sessionId={}", LogTag.WS_PONG, userId, session.getId());
		} catch (Exception e) {
			log.warn("{}pong handle failed sessionId={} reason={}", LogTag.WS_ERR, session.getId(), e.getMessage());
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
				log.debug("{}Session already closed. Clearing up. userId={}", LogTag.WS_SYS, userId);
				sessionManager.forceDisconnect(userId, CloseStatus.SESSION_NOT_RELIABLE);
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
				log.warn("{}Idle session detected. userId={} lastActiveAt={}", LogTag.WS_SYS, userId, lastActiveAt);
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
			log.debug("{}Ping sent successfully. userId={}", LogTag.WS_SYS, userId);
			return true;
		} catch (IOException e) {
			log.warn("{}Ping send failed. userId={} reason={}", LogTag.WS_ERR, userId, e.getMessage());
			return false;
		}
	}
}
