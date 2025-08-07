package com.anonymouschat.anonymouschatserver.common.socket.handler;

import com.anonymouschat.anonymouschatserver.application.event.ChatMessageSaveEvent;
import com.anonymouschat.anonymouschatserver.application.event.ChatMessageSentEvent;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.common.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.common.socket.dto.ChatOutboundMessage;
import com.anonymouschat.anonymouschatserver.common.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.common.socket.support.MessageBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;

import static com.anonymouschat.anonymouschatserver.common.socket.support.WebSocketUtil.extractUserId;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageHandler implements MessageHandler {
	private final ChatSessionManager sessionManager;
	private final ApplicationEventPublisher publisher;
	private final MessageBroadcaster broadcaster;

	@Override
	public MessageType type() {
		return MessageType.CHAT;
	}

	@Override
	public void handle(WebSocketSession session, ChatInboundMessage inbound) {
		try {
			Long senderId = extractUserId(session);
			Long roomId = inbound.roomId();
			String content = inbound.content();

			ChatOutboundMessage outbound = ChatOutboundMessage.builder()
					                               .roomId(roomId)
					                               .type(MessageType.CHAT)
					                               .senderId(senderId)
					                               .content(content)
					                               .timestamp(Instant.now())
					                               .build();

			// 이벤트 발행
			publisher.publishEvent(ChatMessageSentEvent.builder().roomId(roomId).build());
			publisher.publishEvent(ChatMessageSaveEvent.builder()
					                       .chatRoomId(roomId).senderId(senderId).content(content).build());

			// 브로드캐스트
			broadcaster.broadcast(roomId, outbound);
		} catch (Exception e) {
			log.error("메시지 처리 중 예외: {}", e.getMessage(), e);
			sessionManager.forceDisconnect(session, CloseStatus.SERVER_ERROR);
		}
	}
}
