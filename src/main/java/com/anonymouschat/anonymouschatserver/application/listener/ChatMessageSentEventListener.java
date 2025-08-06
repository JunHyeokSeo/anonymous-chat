package com.anonymouschat.anonymouschatserver.application.listener;

import com.anonymouschat.anonymouschatserver.application.event.ChatMessageSentEvent;
import com.anonymouschat.anonymouschatserver.application.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageSentEventListener {

	private final ChatRoomService chatRoomService;

	@EventListener
	public void handle(ChatMessageSentEvent event) {
		try {
			chatRoomService.markActiveIfPending(event.roomId());
		} catch (Exception e) {
			log.warn("채팅방 상태 변경 실패: roomId={} - {}", event.roomId(), e.getMessage());
		}
	}
}