package com.anonymouschat.anonymouschatserver.web.socket.handler;

import com.anonymouschat.anonymouschatserver.web.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.web.socket.support.WsLogTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static com.anonymouschat.anonymouschatserver.web.socket.support.WebSocketUtil.extractUserId;

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
		log.info("{}userId={} roomId={}", WsLogTag.enter(), userId, inbound.roomId());
	}
}
