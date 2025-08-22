package com.anonymouschat.anonymouschatserver.application.listener;

import com.anonymouschat.anonymouschatserver.application.event.ChatPersisted;
import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.ChatOutboundMessage;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.presentation.socket.support.MessageBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatBroadcastListener {
	private final MessageBroadcaster broadcaster;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void on(ChatPersisted event) {
		var outbound = ChatOutboundMessage.builder()
				               .roomId(event.roomId())
				               .type(MessageType.CHAT)
				               .senderId(event.senderId())
				               .content(event.content())
				               .timestamp(java.time.Instant.now())
				               .build();

		log.info("{}채팅 메시지 전송 시도 - roomId={}, senderId={}", LogTag.WS_CHAT, event.roomId(), event.senderId());

		int delivered = broadcaster.broadcast(event.roomId(), outbound);

		if (delivered == 0) {
			log.warn("{}채팅 메시지 수신자 없음 - roomId={}, senderId={}", LogTag.WS_CHAT, event.roomId(), event.senderId());
			// TODO: FCM 이벤트 발행(별도 리스너로 분리)
		} else {
			log.info("{}채팅 메시지 전송 완료 - roomId={}, senderId={}, 수신자 수={}", LogTag.WS_CHAT, event.roomId(), event.senderId(), delivered);
		}
	}
}
