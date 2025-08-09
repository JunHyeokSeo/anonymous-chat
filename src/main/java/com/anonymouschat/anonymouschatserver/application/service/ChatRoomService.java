package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomServiceDto;
import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.repository.ChatRoomRepository;
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
// "대화하기"로 채팅방 생성 뒤 메시지를 입력하지 않은 경우. - 어차피 생성 시 Status 'INACTIVE'
// 한 명이 나간 뒤 나간 사용자가 다시 채팅을 시작할 때. - 채팅을 보낼 때, Status를 Active - > Inactive로 바꾸는데, 이미 Active 이고 내가 나가서 안보였던거라면? returnBy를 통해 채팅 보낸 사용자가 다시 돌아왔음을 알리도록 변경해야됨.
// 한 명이 나간 뒤 나가지 않은 사용자가 다시 채팅을 시작할 때. - 이건 상관 없음. 기존 구조여도 문제 없음
public class ChatRoomService {
	private final ChatRoomRepository chatRoomRepository;

	public ChatRoom createOrFind(User initiator, User recipient) {
		long a = initiator.getId(), b = recipient.getId();
		long left = Math.min(a, b), right = Math.max(a, b);

		// 1) 활성 방 조회
		Optional<ChatRoom> existing = chatRoomRepository.findActiveByPair(left, right);
		if (existing.isPresent()) {
			existing.get().returnBy(initiator.getId());
			return existing.get();
		}

		// 2) 없으면 생성 (경합 시 유니크 충돌 → 재조회)
		try {
			return chatRoomRepository.save(new ChatRoom(initiator, recipient));
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			return chatRoomRepository.findActiveByPair(left, right)
					       .orElseThrow(() -> new IllegalStateException("동시성 충돌 후 활성 방 조회 실패"));
		}
	}

	// 채팅방 상세 조회 (유저 포함 여부 확인 포함)
	@Transactional
	public ChatRoom getVerifiedChatRoomOrThrow(Long userId, Long chatRoomId) {
		ChatRoom chatRoom = findChatRoomById(chatRoomId);
		chatRoom.validateParticipant(userId);
		return chatRoom;
	}

	@Transactional(readOnly = true)
	public List<ChatRoomServiceDto.Summary> getMyActiveChatRooms(Long userId) {
		return chatRoomRepository.findActiveChatRoomsByUser(userId);
	}

	public void exit(Long userId, Long chatRoomId) {
		ChatRoom chatRoom = findChatRoomById(chatRoomId);
		chatRoom.exitBy(userId); // 둘 다 나가면 isActive=false로 전환됨
	}

	private ChatRoom findChatRoomById(Long chatRoomId) {
		return chatRoomRepository.findById(chatRoomId)
				       .orElseThrow(() -> new IllegalStateException("채팅방이 존재하지 않습니다."));
	}

	public void markActiveIfInactive(Long chatRoomId) {
		ChatRoom chatRoom = findChatRoomById(chatRoomId);
		if (!chatRoom.isActive()) {
			chatRoom.activate();
		}
	}

	@Transactional(readOnly = true)
	public boolean isActiveMember(Long chatRoomId, Long userId) {
		return chatRoomRepository.existsByIdAndParticipantIdAndActive(chatRoomId, userId);
	}
}
