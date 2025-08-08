package com.anonymouschat.anonymouschatserver.web.socket;

import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.web.socket.handler.MessageHandler;
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
		MessageType type = inbound.type();
		MessageHandler handler = handlerMap.get(type);
		if (handler == null) {
			log.warn("지원하지 않는 메시지 타입: {} (sessionId={})", type, session.getId());
			return;
		}
		handler.handle(session, inbound);
	}
}
