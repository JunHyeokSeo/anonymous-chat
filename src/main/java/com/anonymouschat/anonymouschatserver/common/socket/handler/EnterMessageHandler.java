package com.anonymouschat.anonymouschatserver.common.socket.handler;

import com.anonymouschat.anonymouschatserver.common.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.common.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.common.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.common.socket.dto.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnterMessageHandler implements MessageHandler {
	private final ChatSessionManager sessionManager;

	@Override
	public MessageType type() {
		return MessageType.ENTER;
	}

	@Override
	public void handle(WebSocketSession session, ChatInboundMessage inbound) {
		Long userId = extractUserId(session);
		sessionManager.joinRoom(inbound.roomId(), userId);
		log.info("[ENTER] userId={} roomId={}", userId, inbound.roomId());
	}

	private Long extractUserId(WebSocketSession session) {
		CustomPrincipal principal = (CustomPrincipal) session.getAttributes().get("principal");
		return principal.userId();
	}
}
