package com.anonymouschat.anonymouschatserver.presentation.socket.handler;

import com.anonymouschat.anonymouschatserver.application.event.ChatSend;
import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import com.anonymouschat.anonymouschatserver.presentation.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.ChatOutboundMessage;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.presentation.socket.support.MessageBroadcaster;
import com.anonymouschat.anonymouschatserver.presentation.socket.support.WebSocketAccessGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;

import static com.anonymouschat.anonymouschatserver.presentation.socket.support.WebSocketUtil.extractUserId;

/**
 * {@link MessageType#CHAT} 타입의 인바운드 메시지를 처리하는 핸들러입니다.
 * 채팅 메시지의 유효성을 검사하고, 메시지를 저장하기 위한 이벤트를 발행하며,
 * 채팅방 참여자들에게 메시지를 브로드캐스트하는 역할을 수행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageHandler implements MessageHandler {
	private final ChatSessionManager sessionManager;
	private final ApplicationEventPublisher publisher;
	private final MessageBroadcaster broadcaster;
	private final WebSocketAccessGuard guard;

	/**
	 * 이 핸들러가 처리할 메시지 타입을 반환합니다.
	 *
	 * @return {@link MessageType#CHAT}
	 */	@Override
	public MessageType type() {
		return MessageType.CHAT;
	}

	/**
	 * 수신된 채팅 메시지를 처리합니다.
	 * 1. 사용자가 채팅방 참여자인지 확인합니다.
	 * 2. 아웃바운드 메시지 객체를 생성합니다.
	 * 3. 메시지 저장을 위한 {@link ChatSend} 이벤트를 발행합니다.
	 * 4. 채팅방 참여자들에게 메시지를 브로드캐스트합니다.
	 * 5. 처리 중 예외 발생 시 세션을 종료합니다.
	 *
	 * @param session 메시지를 보낸 WebSocket 세션
	 * @param inbound 처리할 인바운드 메시지 객체
	 */	@Override
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

			// 이벤트 발행: 메시지 저장 로직은 이벤트 리스너에서 비동기적으로 처리됩니다.
			publisher.publishEvent(ChatSend.builder().roomId(roomId).senderId(senderId).content(content).build());

			// 로깅
			log.info("{}roomId={} senderId={} ts={}", LogTag.WS_CHAT, roomId, senderId, outbound.timestamp());

			// 브로드캐스트: 채팅방 참여자들에게 메시지를 전송합니다.
			int delivered = broadcaster.broadcast(roomId, outbound);
			if (delivered == 0) {
				log.warn("{}no receiver online: roomId={} senderId={}", LogTag.WS_BROADCAST, roomId, senderId);
				//todo: FCM 이벤트 발행
			}
		} catch (Exception e) {
			log.error("{}message handling error: {}", LogTag.WS_ERR, e.getMessage(), e);
			sessionManager.forceDisconnect(session, CloseStatus.SERVER_ERROR);
		}
	}
}
