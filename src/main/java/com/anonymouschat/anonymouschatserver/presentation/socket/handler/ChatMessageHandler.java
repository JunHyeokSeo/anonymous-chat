package com.anonymouschat.anonymouschatserver.presentation.socket.handler;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.event.PushNotificationRequired;
import com.anonymouschat.anonymouschatserver.application.event.MessageStoreFailure;
import com.anonymouschat.anonymouschatserver.application.usecase.MessageUseCase;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;

import static com.anonymouschat.anonymouschatserver.presentation.socket.support.WebSocketUtil.extractUserId;

/**
 * {@link MessageType#CHAT} 타입의 인바운드 메시지를 처리하는 핸들러입니다.
 * 실시간성을 위해 브로드캐스트를 먼저 수행하고, 메시지 저장은 비동기로 처리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageHandler implements MessageHandler {
	private final ChatSessionManager sessionManager;
	private final MessageBroadcaster broadcaster;
	private final WebSocketAccessGuard guard;
	private final MessageUseCase messageUseCase;
	private final ApplicationEventPublisher publisher;

	@Override
	public MessageType type() {
		return MessageType.CHAT;
	}

	/**
	 * 수신된 채팅 메시지를 처리합니다.
	 * 1. 사용자가 채팅방 참여자인지 확인합니다.
	 * 2. 즉시 채팅방 참여자들에게 메시지를 브로드캐스트합니다.
	 * 3. 메시지 저장을 비동기로 처리합니다.
	 * 4. 처리 중 예외 발생 시 세션을 종료합니다.
	 */
	@Override
	public void handle(WebSocketSession session, ChatInboundMessage inbound) {
		try {
			Long roomId = inbound.roomId();
			Long senderId = extractUserId(session);
			String content = inbound.content();

			if (!guard.ensureParticipant(session, roomId, senderId)) {
				return;
			}

			Instant timestamp = Instant.now();

			// 1. 즉시 브로드캐스트 (실시간성 우선)
			ChatOutboundMessage outbound = ChatOutboundMessage.builder()
					                               .roomId(roomId)
					                               .type(MessageType.CHAT)
					                               .senderId(senderId)
					                               .content(content)
					                               .timestamp(timestamp)
					                               .build();

			int delivered = broadcaster.broadcastExcept(roomId, outbound, senderId);

			log.info("{}message broadcasted: roomId={} senderId={} delivered={}",
					LogTag.WS_CHAT, roomId, senderId, delivered);

			// 2. 비동기로 메시지 저장
			saveMessageAsync(roomId, senderId, content, delivered == 0);

		} catch (Exception e) {
			log.error("{}message handling error: {}", LogTag.WS_ERR, e.getMessage(), e);
			sessionManager.forceDisconnect(session, CloseStatus.SERVER_ERROR);
		}
	}

	/**
	 * 메시지를 비동기적으로 저장합니다.
	 * 저장 성공 시: 온라인 수신자가 없으면 푸시 알림 이벤트 발행
	 * 저장 실패 시: 재시도 또는 모니터링을 위한 실패 이벤트 발행
	 */
	@Async
	void saveMessageAsync(Long roomId, Long senderId, String content, boolean noOnlineReceivers) {
		try {
			Long messageId = messageUseCase.sendMessage(MessageUseCaseDto.SendMessageRequest.builder()
					                                            .roomId(roomId)
					                                            .senderId(senderId)
					                                            .content(content)
					                                            .build());

			log.debug("{}message saved successfully: roomId={} senderId={} messageId={}",
					LogTag.CHAT, roomId, senderId, messageId);

			// FCM 전송이 필요한 경우에만 이벤트 발행
			if (noOnlineReceivers) {
				publisher.publishEvent(PushNotificationRequired.of(roomId, senderId, messageId, content));
				log.info("{}push notification event published: roomId={} messageId={}",
						LogTag.CHAT, roomId, messageId);
			}

		} catch (Exception e) {
			log.error("{}message save failed: roomId={} senderId={} error={}",
					LogTag.CHAT, roomId, senderId, e.getMessage());

			// 저장 실패 이벤트 발행 (재시도 또는 알림용)
			publisher.publishEvent(new MessageStoreFailure(roomId, senderId, content));
		}
	}
}