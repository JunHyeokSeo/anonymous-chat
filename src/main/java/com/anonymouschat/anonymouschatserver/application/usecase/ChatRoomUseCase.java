package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomServiceDto;
import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.service.ChatRoomService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.annotation.UseCase;
import com.anonymouschat.anonymouschatserver.common.log.LogTag;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class ChatRoomUseCase {
	private final ChatRoomService chatRoomService;
	private final UserService userService;

	/**
	 * 채팅방을 생성하거나 기존 채팅방을 반환합니다.
	 */
	public Long createOrFind(Long initiatorId, Long recipientId) {
		User initiator = userService.findUser(initiatorId);
		User recipient = userService.findUser(recipientId);
		return chatRoomService.createOrFind(initiator, recipient).getId();
	}

	/**
	 * 본인이 참여 중인 활성 채팅방 목록을 조회합니다.
	 */
	public List<ChatRoomUseCaseDto.Summary> getMyActiveChatRooms(Long userId) {
		List<ChatRoomServiceDto.Summary> result = chatRoomService.getMyActiveChatRooms(userId);
		return result.stream()
				       .map(ChatRoomUseCaseDto.Summary::from)
				       .toList();
	}

	/**
	 * 채팅방을 나갑니다.
	 */
	public void exitChatRoom(Long userId, Long roomId){
		chatRoomService.exit(userId, roomId);
	}
}
