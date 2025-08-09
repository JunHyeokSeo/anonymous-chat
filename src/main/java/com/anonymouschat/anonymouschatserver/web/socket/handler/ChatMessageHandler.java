package com.anonymouschat.anonymouschatserver.web.socket.handler;

import com.anonymouschat.anonymouschatserver.application.event.ChatSave;
import com.anonymouschat.anonymouschatserver.web.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatOutboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.web.socket.support.MessageBroadcaster;
import com.anonymouschat.anonymouschatserver.web.socket.support.WebSocketAccessGuard;
import com.anonymouschat.anonymouschatserver.web.socket.support.WsLogTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;

import static com.anonymouschat.anonymouschatserver.web.socket.support.WebSocketUtil.extractUserId;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageHandler implements MessageHandler {
	private final ChatSessionManager sessionManager;
	private final ApplicationEventPublisher publisher;
	private final MessageBroadcaster broadcaster;
	private final WebSocketAccessGuard guard;

	@Override
	public MessageType type() {
		return MessageType.CHAT;
	}

	@Override
	public void handle(WebSocketSession session, ChatInboundMessage inbound) {
		try {
			Long roomId = inbound.roomId();
			Long senderId = extractUserId(session);
			String content = inbound.content();

			if (!guard.ensureParticipant(session, roomId, senderId))
				return;

			ChatOutboundMessage outbound = ChatOutboundMessage.builder()
					                               .roomId(roomId)
					                               .type(MessageType.CHAT)
					                               .senderId(senderId)
					                               .content(content)
					                               .timestamp(Instant.now())
					                               .build();

			// 이벤트 발행
			publisher.publishEvent(ChatSave.builder().roomId(roomId).senderId(senderId).content(content).build());

			// 로깅
			log.info("{}roomId={} senderId={} ts={}", WsLogTag.chat(), roomId, senderId, outbound.timestamp());

			// 브로드캐스트
			int delivered = broadcaster.broadcast(roomId, outbound);
			if (delivered == 0) {
				log.warn("{}no receiver online: roomId={} senderId={}", WsLogTag.bc(), roomId, senderId);
				//todo: FCM 이벤트 발행
			}
		} catch (Exception e) {
			log.error("메시지 처리 중 예외: {}", e.getMessage(), e);
			sessionManager.forceDisconnect(session, CloseStatus.SERVER_ERROR);
		}
	}
}
