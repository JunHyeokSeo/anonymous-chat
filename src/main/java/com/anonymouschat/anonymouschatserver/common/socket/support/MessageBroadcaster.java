package com.anonymouschat.anonymouschatserver.common.socket.support;

import com.anonymouschat.anonymouschatserver.common.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.common.socket.dto.ChatOutboundMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageBroadcaster {
	private final ChatSessionManager sessionManager;
	private final ObjectMapper objectMapper;

	public void broadcast(Long roomId, ChatOutboundMessage message) {
		try {
			String payload = objectMapper.writeValueAsString(message);
			for (Long participantId : sessionManager.getParticipants(roomId)) {
				WebSocketSession session = sessionManager.getSession(participantId);
				if (session != null && session.isOpen()) {
					session.sendMessage(new TextMessage(payload));
				}
			}
		} catch (Exception e) {
			log.error("브로드캐스트 실패: {}", e.getMessage(), e);
		}
	}
}
