package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomServiceDto;
import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomSummaryResult;
import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.service.ChatRoomService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.annotation.UseCase;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class ChatRoomUseCase {
	private final ChatRoomService chatRoomService;
	private final UserService userService;

	public Long createOrFind(Long initiatorId, Long recipientId) {
		User initiator = userService.findUser(initiatorId);
		User recipient = userService.findUser(recipientId);
		return chatRoomService.createOrFind(initiator, recipient).getId();
	}

	public List<ChatRoomUseCaseDto.Summary> getMyActiveChatRooms(Long userId) {
		List<ChatRoomServiceDto.Summary> result = chatRoomService.getMyActiveChatRooms(userId);
		return result.stream()
				       .map(ChatRoomUseCaseDto.Summary::from)
				       .toList();
	}

	public void exitChatRoom(Long userId, Long chatRoomId){
		chatRoomService.exit(userId, chatRoomId);
	}
}
