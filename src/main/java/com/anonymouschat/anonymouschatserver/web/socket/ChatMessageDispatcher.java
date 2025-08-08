package com.anonymouschat.anonymouschatserver.web.socket;

import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.web.socket.handler.MessageHandler;
import com.anonymouschat.anonymouschatserver.web.socket.support.WsLogTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ChatMessageDispatcher {
	private final Map<MessageType, MessageHandler> handlerMap;

	public ChatMessageDispatcher(List<MessageHandler> handlers) {
		this.handlerMap = handlers.stream()
				                  .collect(Collectors.toUnmodifiableMap(MessageHandler::type, h -> h));
	}

	public void dispatch(WebSocketSession session, ChatInboundMessage inbound) {
		validateInbound(inbound);
		MessageHandler handler = handlerMap.get(inbound.type());
		if (handler == null) {
			throw new UnsupportedOperationException("Unsupported message type: " + inbound.type());
		}
		log.debug("{}type={} roomId={} sessionId={}", WsLogTag.sys(), inbound.type(), inbound.roomId(), session.getId());
		handler.handle(session, inbound);
	}

	private void validateInbound(ChatInboundMessage inbound) {
		if (inbound == null) {
			throw new IllegalArgumentException("Inbound payload is required.");
		}
		if (inbound.type() == null) {
			throw new IllegalArgumentException("Inbound.type is required.");
		}
		if (inbound.roomId() == null) {
			throw new IllegalArgumentException("Inbound.roomId is required.");
		}
		if (inbound.type() == MessageType.CHAT && (inbound.content() == null || inbound.content().isBlank())) {
		     throw new IllegalArgumentException("Inbound.content is required for CHAT.");
		}
	}
}
