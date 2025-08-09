package com.anonymouschat.anonymouschatserver.web.socket.handler;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto.MarkMessagesAsRead;
import com.anonymouschat.anonymouschatserver.application.usecase.MessageUseCase;
import com.anonymouschat.anonymouschatserver.web.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatOutboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.web.socket.support.MessageBroadcaster;
import com.anonymouschat.anonymouschatserver.web.socket.support.WebSocketAccessGuard;
import com.anonymouschat.anonymouschatserver.web.socket.support.WsLogTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;

import static com.anonymouschat.anonymouschatserver.web.socket.support.WebSocketUtil.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadMessageHandler implements MessageHandler{
	private final ChatSessionManager sessionManager;
	private final MessageUseCase messageUseCase;
	private final MessageBroadcaster broadcaster;
	private final WebSocketAccessGuard guard;

	@Override
	public MessageType type() {
		return MessageType.READ;
	}

	@Override
	public void handle(WebSocketSession session, ChatInboundMessage inbound) {
		Long userId = extractUserId(session);
		Long roomId = inbound.roomId();

		if (!guard.ensureParticipant(session, roomId, userId))
			return;

		try {
			// 메시지 읽음 처리
			Long lastReadMessageId = messageUseCase.markMessagesAsRead(
					MarkMessagesAsRead.builder()
							.userId(userId)
							.roomId(roomId)
							.build()
			);

			if (lastReadMessageId != null) {
				ChatOutboundMessage outbound = ChatOutboundMessage.builder()
						                               .roomId(roomId)
						                               .type(MessageType.READ)
						                               .senderId(userId)
						                               .lastReadMessageId(lastReadMessageId)
						                               .timestamp(Instant.now())
						                               .build();

				//로깅
				log.info("{}roomId={} userId={} lastReadMessageId={}", WsLogTag.read(), roomId, userId, lastReadMessageId);

				// 브로드 캐스트
				broadcaster.broadcastExcept(roomId, outbound, userId);
			}
		} catch (Exception e) {
			log.error("메시지 읽음 처리 중 오류 발생: userId={}, roomId={}, error={}", userId, roomId, e.getMessage(), e);
			sessionManager.forceDisconnect(session, CloseStatus.SERVER_ERROR);
		}
	}
}
