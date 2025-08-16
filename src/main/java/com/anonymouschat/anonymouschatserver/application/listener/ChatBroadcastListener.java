package com.anonymouschat.anonymouschatserver.application.listener;

import com.anonymouschat.anonymouschatserver.application.event.ChatPersisted;
import com.anonymouschat.anonymouschatserver.common.log.LogTag;
import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatOutboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.web.socket.support.MessageBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
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

		int delivered = broadcaster.broadcast(event.roomId(), outbound);
		if (delivered == 0) {
			log.warn("{}no receiver online roomId={} senderId={}", LogTag.WS_CHAT, event.roomId(), event.senderId());
			// TODO: FCM 이벤트 발행(별도 리스너로 분리 권장)
		}
	}
}
