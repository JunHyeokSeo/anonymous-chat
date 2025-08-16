package com.anonymouschat.anonymouschatserver.application.listener;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.event.ChatPersisted;
import com.anonymouschat.anonymouschatserver.application.event.ChatSave;
import com.anonymouschat.anonymouschatserver.application.service.ChatRoomService;
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
public class ChatSendOrchestrator {
	private final ChatRoomService chatRoomService;
	private final MessageUseCase messageUseCase;
	private final ApplicationEventPublisher publisher;

	@EventListener
	@Transactional
	public void on(ChatSave event) {
        log.info("{}Received ChatSave event. roomId={}, senderId={}", LogTag.ORCHESTRATOR, event.roomId(), event.senderId());
		// 채팅방 isActive가 false 라면 true 로 변경
		chatRoomService.markActiveIfInactive(event.roomId());

		// returnBy (CHAT 시점 채팅방 복귀)
		chatRoomService.returnBy(event.roomId(), event.senderId());

		// 메시지 저장
		Long messageId = messageUseCase.sendMessage(MessageUseCaseDto.SendMessage.builder()
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
