package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomServiceDto;
import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.service.ChatRoomService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.annotation.UseCase;
import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.entity.UserProfileImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class ChatRoomUseCase {
	private final ChatRoomService chatRoomService;
	private final UserService userService;

	@Transactional
	public Long createOrFind(Long initiatorId, Long recipientId) {
		User initiator = userService.findUser(initiatorId);
		User recipient = userService.findUser(recipientId);
		return chatRoomService.createOrFind(initiator, recipient).getId();
	}

	@Transactional(readOnly = true)
	public ChatRoomUseCaseDto.SummaryResponse getChatRoom(Long chatRoomId, Long userId) {
		ChatRoom room = chatRoomService.findChatRoomById(chatRoomId);
		User opponent = room.getUser1().getId().equals(userId) ? room.getUser1() : room.getUser2();
		return ChatRoomUseCaseDto.SummaryResponse.builder()
				       .roomId(room.getId())
				       .opponentId(opponent.getId())
				       .opponentNickname(opponent.getNickname())
				       .opponentAge(opponent.getAge())
				       .opponentRegion(opponent.getRegion().name())
				       .opponentProfileImageUrl(opponent.getProfileImages().stream().filter(UserProfileImage::isRepresentative).findFirst().map(UserProfileImage::getImageUrl).orElse(null))
				       .lastMessageTime(room.getUpdatedAt())
				       .build();
	}

	@Transactional(readOnly = true)
	public List<ChatRoomUseCaseDto.SummaryResponse> getMyActiveChatRooms(Long userId) {
		List<ChatRoomServiceDto.SummaryResult> result = chatRoomService.getMyActiveChatRooms(userId);
		return result.stream()
				       .map(ChatRoomUseCaseDto.SummaryResponse::from)
				       .toList();
	}

	@Transactional
	public void exitChatRoom(Long userId, Long roomId){
		ChatRoom chatRoom = chatRoomService.getVerifiedChatRoomOrThrow(userId, roomId);
		chatRoomService.exit(userId, chatRoom);
	}
}
