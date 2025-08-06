package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomServiceDto;
import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.repository.ChatRoomRepository;
import com.anonymouschat.anonymouschatserver.domain.type.ChatRoomStatus;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
//todo: 채팅방 상태 관리 구체화 필요
// "대화하기"로 채팅방 생성 뒤 메시지를 입력하지 않은 경우.
// 한 명이 나간 뒤 나간 사용자가 다시 채팅을 시작할 때.
// 한 명이 나간 뒤 나가지 않은 사용자가 다시 채팅을 시작할 때.
public class ChatRoomService {
	private final ChatRoomRepository chatRoomRepository;

	// 채팅방 생성 또는 기존 방 조회
	public ChatRoom createOrFind(User initiator, User recipient) {
		Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findByParticipantsAndStatus(initiator.getId(), recipient.getId(), ChatRoomStatus.ACTIVE);

		if (chatRoomOptional.isPresent()) {
			ChatRoom savedChatRoom = chatRoomOptional.get();
			savedChatRoom.returnBy(initiator.getId());
			return savedChatRoom;
		}

		return chatRoomRepository.save(new ChatRoom(initiator, recipient));
	}

	// 채팅방 상세 조회 (유저 포함 여부 확인 포함)
	@Transactional
	public ChatRoom getVerifiedChatRoomOrThrow(Long userId, Long chatRoomId) {
		ChatRoom chatRoom = findChatRoomById(chatRoomId);
		chatRoom.validateParticipant(userId);

		return chatRoom;
	}

	// 내가 참여한 채팅방 목록 조회
	@Transactional(readOnly = true)
	public List<ChatRoomServiceDto.Summary> getMyActiveChatRooms(Long userId) {
		return chatRoomRepository.findActiveChatRoomsByUser(userId, ChatRoomStatus.ACTIVE);
	}

	// 채팅방 나가기
	public void exit(Long userId, Long chatRoomId) {
		ChatRoom chatRoom = findChatRoomById(chatRoomId);
		chatRoom.exitBy(userId);
	}

	private ChatRoom findChatRoomById(Long chatRoomId) {
		return chatRoomRepository
				       .findById(chatRoomId)
				       .orElseThrow(() -> new IllegalStateException("채팅방이 존재하지 않습니다."));
	}

	public void markActiveIfInactive(Long chatRoomId) {
		ChatRoom chatRoom = findChatRoomById(chatRoomId);

		if (chatRoom.getStatus() == ChatRoomStatus.INACTIVE) {
			chatRoom.activate();
			chatRoomRepository.save(chatRoom);
		}
	}
}
