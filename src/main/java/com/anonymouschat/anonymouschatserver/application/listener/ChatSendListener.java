package com.anonymouschat.anonymouschatserver.application.listener;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.event.ChatPersisted;
import com.anonymouschat.anonymouschatserver.application.event.ChatSend;
import com.anonymouschat.anonymouschatserver.application.usecase.MessageUseCase;
import com.anonymouschat.anonymouschatserver.common.log.LogTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatSendListener {
	private final MessageUseCase messageUseCase;
	private final ApplicationEventPublisher publisher;

	@EventListener
	@Transactional
	public void on(ChatSend event) {
        log.info("{}ChatSend 이벤트 수신: roomId={}, senderId={}", LogTag.CHAT, event.roomId(), event.senderId());
		// 메시지 저장
		Long messageId = messageUseCase.sendMessage(MessageUseCaseDto.SendMessageRequest.builder()
				                                    .roomId(event.roomId())
				                                    .senderId(event.senderId())
				                                    .content(event.content())
				                                    .build());

		// 저장 완료 이벤트 발행 (동일 트랜잭션에서 발행됨)
		publisher.publishEvent(ChatPersisted.builder()
				                       .roomId(event.roomId())
				                       .senderId(event.senderId())
				                       .messageId(messageId)
				                       .content(event.content())
				                       .createdAt(Instant.now())
				                       .build());
	}
}
