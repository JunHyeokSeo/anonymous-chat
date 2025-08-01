package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.*;
import com.anonymouschat.anonymouschatserver.application.service.ChatRoomService;
import com.anonymouschat.anonymouschatserver.application.service.MessageService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.annotation.UseCase;
import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.entity.Message;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class MessageUseCase {
	private final MessageService messageService;
	private final ChatRoomService chatRoomService;
	private final UserService userService;

	// 메시지 전송
	public void sendMessage(MessageUseCaseDto.SendMessage request) {
		ChatRoom chatRoom = chatRoomService.getVerifiedChatRoomOrThrow(request.senderId(), request.chatRoomId());
		User sender = userService.findUser(request.senderId());
		messageService.saveMessage(chatRoom, sender, request.content());
	}

	// 메시지 목록 조회 (Cursor 방식)
	public List<MessageUseCaseDto.MessageResult> getMessages(MessageUseCaseDto.GetMessages request) {
		ChatRoom chatRoom = chatRoomService.getVerifiedChatRoomOrThrow(request.userId(), request.chatRoomId());
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


	// 메시지 읽음 처리
	public void markMessagesAsRead(MessageUseCaseDto.MarkMessagesAsRead request) {
		chatRoomService.getVerifiedChatRoomOrThrow(request.userId(), request.chatRoomId());
		messageService.markMessagesAsRead(request.chatRoomId(), request.userId());
	}
}
