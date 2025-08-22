package com.anonymouschat.anonymouschatserver.presentation.socket.support;

import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import com.anonymouschat.anonymouschatserver.presentation.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.ChatOutboundMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.stereotype.Component;

/**
 * WebSocket 메시지를 채팅방 참여자들에게 브로드캐스트하는 기능을 제공합니다.
 * 메시지 직렬화 및 전송 실패 시 세션 정리 로직을 포함합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageBroadcaster {
	private final ChatSessionManager sessionManager;
	private final ObjectMapper objectMapper;

	/**
	 * 특정 채팅방의 모든 참여자에게 메시지를 브로드캐스트합니다.
	 * {@link #broadcastExcept(Long, ChatOutboundMessage, Long)}를 사용하여 특정 제외 사용자가 없는 경우로 처리합니다.
	 *
	 * @param roomId 메시지를 보낼 채팅방 ID
	 * @param message 브로드캐스트할 메시지 객체
	 * @return 메시지 전송에 성공한 참여자 수
	 */
	public int broadcast(Long roomId, ChatOutboundMessage message) {
		return broadcastExcept(roomId, message, null);
	}

	/**
	 * 특정 채팅방의 참여자들에게 메시지를 브로드캐스트하되, 특정 사용자는 제외합니다.
	 * 메시지 직렬화에 실패하거나, 세션이 유효하지 않은 경우 해당 세션을 정리합니다.
	 *
	 * @param roomId 메시지를 보낼 채팅방 ID
	 * @param message 브로드캐스트할 메시지 객체
	 * @param excludeUserId 메시지 수신에서 제외할 사용자 ID (null인 경우 모든 참여자에게 전송)
	 * @return 메시지 전송에 성공한 참여자 수
	 */
	public int broadcastExcept(Long roomId, ChatOutboundMessage message, Long excludeUserId) {
		int success = 0;
		String payload;
		try {
			payload = objectMapper.writeValueAsString(message);
		} catch (Exception e) {
			log.error("{}serialize failed roomId={} reason={}", LogTag.WS_BROADCAST, roomId, e.getMessage(), e);
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

	/**
	 * 단일 참여자에게 WebSocket 메시지를 전송합니다.
	 * 세션이 없거나 닫혀있거나, 메시지 전송 중 예외가 발생하면 해당 세션을 정리합니다.
	 *
	 * @param participantId 메시지를 받을 참여자 ID
	 * @param payload 전송할 메시지 페이로드 (JSON 문자열)
	 * @return 메시지 전송 성공 여부
	 */
	private boolean sendToParticipant(Long participantId, String payload) {
		WebSocketSession session = sessionManager.getSession(participantId);
		if (session == null) {
			log.debug("{}skip(no session) userId={}", LogTag.WS_BROADCAST, participantId);
			sessionManager.forceDisconnect(participantId, CloseStatus.SESSION_NOT_RELIABLE);
			return false;
		}
		if (!session.isOpen()) {
			log.debug("{}skip(closed) userId={}", LogTag.WS_BROADCAST, participantId);
			sessionManager.forceDisconnect(participantId, CloseStatus.SESSION_NOT_RELIABLE);
			return false;
		}

		try {
			session.sendMessage(new TextMessage(payload));
			return true;
		} catch (Exception e) {
			log.warn("{}send failed userId={} sessionId={} reason={}", LogTag.WS_BROADCAST, participantId, safeSessionId(session), e.getMessage());
			sessionManager.forceDisconnect(participantId, CloseStatus.SESSION_NOT_RELIABLE);
			return false;
		}
	}

	/**
	 * WebSocketSession의 ID를 안전하게 반환합니다. 예외 발생 시 "unknown"을 반환합니다.
	 *
	 * @param s WebSocketSession 객체
	 * @return 세션 ID 또는 "unknown"
	 */	private String safeSessionId(WebSocketSession s) {
		try { return s.getId(); } catch (Exception ignored) { return "unknown"; }
	}
}
