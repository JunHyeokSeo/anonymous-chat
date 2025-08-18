package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class MessageUseCase {
	private final MessageService messageService;
	private final ChatRoomService chatRoomService;
	private final UserService userService;

	@Transactional
	public Long sendMessage(MessageUseCaseDto.SendMessageRequest request) {
		log.info("{}메시지 전송 요청 - senderId={}, roomId={}",
				LogTag.MESSAGE, request.senderId(), request.roomId());

		ChatRoom chatRoom = chatRoomService.getVerifiedChatRoomOrThrow(
				request.senderId(), request.roomId());

		chatRoomService.markActiveIfInactive(chatRoom);
		chatRoomService.returnBy(chatRoom, request.senderId());

		User sender = userService.findUser(request.senderId());
		Long messageId = messageService.saveMessage(chatRoom, sender, request.content()).getId();

		log.info("{}메시지 전송 완료 - messageId={}", LogTag.MESSAGE, messageId);
		return messageId;
	}

	@Transactional(readOnly = true)
	public List<MessageUseCaseDto.MessageResponse> getMessages(MessageUseCaseDto.GetMessagesRequest request) {
		ChatRoom chatRoom = chatRoomService.getVerifiedChatRoomOrThrow(request.userId(), request.roomId());
		LocalDateTime lastExitedAt = chatRoom.getLastExitedAt(request.userId());

		List<Message> messages = messageService.getMessages(chatRoom, lastExitedAt, request.lastMessageId(), request.limit());

		return messages.stream()
				       .map(m -> MessageUseCaseDto.MessageResponse.from(m, request.userId()))
				       .toList();
	}

	@Transactional
	public Long markMessagesAsRead(MessageUseCaseDto.MarkMessagesAsReadRequest request) {
		chatRoomService.getVerifiedChatRoomOrThrow(request.userId(), request.roomId());
		return messageService.markMessagesAsRead(request.roomId(), request.userId());
	}

	@Transactional(readOnly = true)
	public Long getLastReadMessageIdByOpponent(MessageUseCaseDto.GetLastReadMessageRequest request) {
		chatRoomService.getVerifiedChatRoomOrThrow(request.userId(), request.roomId());
		return messageService.findLastReadMessageIdByReceiver(request.roomId(), request.userId());
	}
}
