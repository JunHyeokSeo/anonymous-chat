package com.anonymouschat.anonymouschatserver.application.usecase.message;

import com.anonymouschat.anonymouschatserver.application.dto.*;
import com.anonymouschat.anonymouschatserver.application.service.chatroom.ChatRoomService;
import com.anonymouschat.anonymouschatserver.application.service.message.MessageService;
import com.anonymouschat.anonymouschatserver.application.service.user.UserService;
import com.anonymouschat.anonymouschatserver.common.annotation.UseCase;
import com.anonymouschat.anonymouschatserver.domain.chatroom.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.message.entity.Message;
import com.anonymouschat.anonymouschatserver.domain.user.entity.User;
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
	public MessageResult sendMessage(SendMessageCommand command) {
		ChatRoom chatRoom = chatRoomService.getChatRoomDetail(command.senderId(), command.chatRoomId());
		User sender = userService.findUser(command.senderId());
		Message message = messageService.sendMessage(chatRoom, sender, command.content());

		return toResult(message, command.senderId());
	}

	// 메시지 목록 조회 (Cursor 방식)
	public List<MessageResult> getMessages(GetMessagesCommand command) {
		ChatRoom chatRoom = chatRoomService.getChatRoomDetail(command.userId(), command.chatRoomId());
		LocalDateTime lastExitedAt = chatRoom.getLastExitedAt(command.userId());

		List<Message> messages = messageService.getMessages(
				chatRoom,
				lastExitedAt,
				command.lastMessageId(),
				command.limit()
		);

		return messages.stream()
				       .map(m -> toResult(m, command.userId()))
				       .toList();
	}


	// 메시지 읽음 처리
	public void markMessagesAsRead(MarkMessagesAsReadCommand command) {
		chatRoomService.getChatRoomDetail(command.userId(), command.chatRoomId());
		messageService.markMessagesAsRead(command.chatRoomId(), command.userId());
	}

	private MessageResult toResult(Message message, Long userId) {
		boolean isMine = message.isSentBy(userId);
		return new MessageResult(
				message.getId(),
				message.getChatRoom().getId(),
				message.getSender().getId(),
				message.getContent(),
				message.isRead(),
				message.getSentAt(),
				isMine
		);
	}
}
