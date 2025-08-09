package com.anonymouschat.anonymouschatserver.web.socket.support;

import com.anonymouschat.anonymouschatserver.web.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatOutboundMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageBroadcaster {
	private final ChatSessionManager sessionManager;
	private final ObjectMapper objectMapper;

	/** 방 전체 브로드캐스트 */
	public int broadcast(Long roomId, ChatOutboundMessage message) {
		return broadcastExcept(roomId, message, null);
	}

	/** 특정 유저(excludeUserId)만 제외하고 브로드캐스트 */
	public int broadcastExcept(Long roomId, ChatOutboundMessage message, Long excludeUserId) {
		int success = 0;
		String payload;
		try {
			payload = objectMapper.writeValueAsString(message);
		} catch (Exception e) {
			log.error("{}serialize failed roomId={} reason={}", WsLogTag.bc(), roomId, e.getMessage(), e);
			return 0;
		}

		for (Long participantId : sessionManager.getParticipants(roomId)) {
			if (excludeUserId != null && excludeUserId.equals(participantId)) continue;
			if (sendToParticipant(participantId, payload)) {
				success++;
			}
		}
		return success;
	}

	/** 단일 대상 전송 + 실패 시 정리 */
	private boolean sendToParticipant(Long participantId, String payload) {
		WebSocketSession session = sessionManager.getSession(participantId);
		if (session == null) {
			log.debug("{}skip(no session) userId={}", WsLogTag.bc(), participantId);
			sessionManager.forceDisconnect(participantId, CloseStatus.SESSION_NOT_RELIABLE);
			return false;
		}
		if (!session.isOpen()) {
			log.debug("{}skip(closed) userId={}", WsLogTag.bc(), participantId);
			sessionManager.forceDisconnect(participantId, CloseStatus.SESSION_NOT_RELIABLE);
			return false;
		}

		try {
			session.sendMessage(new TextMessage(payload));
			return true;
		} catch (Exception e) {
			log.warn("{}send failed userId={} sessionId={} reason={}", WsLogTag.bc(), participantId, safeSessionId(session), e.getMessage());
			sessionManager.forceDisconnect(participantId, CloseStatus.SESSION_NOT_RELIABLE);
			return false;
		}
	}

	private String safeSessionId(WebSocketSession s) {
		try { return s.getId(); } catch (Exception ignored) { return "unknown"; }
	}
}
