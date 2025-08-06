package com.anonymouschat.anonymouschatserver.application.listener;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.event.ChatMessageSaveEvent;
import com.anonymouschat.anonymouschatserver.application.usecase.MessageUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageSaveEventListener {

	private final MessageUseCase messageUseCase;

	@Async
	@EventListener
	public void handle(ChatMessageSaveEvent event) {
		var dto = MessageUseCaseDto.SendMessage.builder()
				          .chatRoomId(event.chatRoomId())
				          .senderId(event.senderId())
				          .content(event.content())
				          .build();

		messageUseCase.sendMessage(dto);
	}
}
