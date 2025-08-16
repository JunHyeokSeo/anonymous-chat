package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.*;
import com.anonymouschat.anonymouschatserver.application.service.ChatRoomService;
import com.anonymouschat.anonymouschatserver.application.service.MessageService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.annotation.UseCase;
import com.anonymouschat.anonymouschatserver.common.log.LogTag;
import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.entity.Message;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class MessageUseCase {
	private final MessageService messageService;
	private final ChatRoomService chatRoomService;
	private final UserService userService;

	/**
	 * 메시지를 전송합니다.
	 */
	public Long sendMessage(MessageUseCaseDto.SendMessage request) {
		log.info("{}메시지 전송 요청 - senderId={}, roomId={}", LogTag.MESSAGE, request.senderId(), request.roomId());
		ChatRoom chatRoom = chatRoomService.getVerifiedChatRoomOrThrow(request.senderId(), request.roomId());
		User sender = userService.findUser(request.senderId());
		Long messageId = messageService.saveMessage(chatRoom, sender, request.content()).getId();
		log.info("{}메시지 전송 완료 - messageId={}", LogTag.MESSAGE, messageId);
		return messageId;
	}

	/**
	 * 메시지 목록을 조회합니다. (커서 기반 페이징)
	 */
	public List<MessageUseCaseDto.MessageResult> getMessages(MessageUseCaseDto.GetMessages request) {
		ChatRoom chatRoom = chatRoomService.getVerifiedChatRoomOrThrow(request.userId(), request.roomId());
		LocalDateTime lastExitedAt = chatRoom.getLastExitedAt(request.userId());

		List<Message> messages = messageService.getMessages(
				chatRoom,
				lastExitedAt,
				request.lastMessageId(),
				request.limit()
		);

		return messages.stream()
				       .map(m -> MessageUseCaseDto.MessageResult.from(m, request.userId()))
				       .toList();
	}

	/**
	 * 메시지를 읽음 처리합니다.
	 */
	public Long markMessagesAsRead(MessageUseCaseDto.MarkMessagesAsRead request) {
		chatRoomService.getVerifiedChatRoomOrThrow(request.userId(), request.roomId());
		return messageService.markMessagesAsRead(request.roomId(), request.userId());
	}

	/**
	 * 상대방이 마지막으로 읽은 메시지 ID를 조회합니다.
	 */
	public Long getLastReadMessageIdByOpponent(MessageUseCaseDto.GetLastReadMessage request) {
		chatRoomService.getVerifiedChatRoomOrThrow(request.userId(), request.roomId());
		return messageService.findLastReadMessageIdByReceiver(request.roomId(), request.userId());
	}
}