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
	public ChatRoom getVerifiedChatRoomOrThrow(Long userId, Long roomId) {
		ChatRoom chatRoom = findChatRoomById(roomId);
		chatRoom.validateParticipant(userId);
		return chatRoom;
	}

	@Transactional(readOnly = true)
	public List<ChatRoomServiceDto.Summary> getMyActiveChatRooms(Long userId) {
		return chatRoomRepository.findActiveChatRoomsByUser(userId);
	}

	public void exit(Long userId, Long roomId) {
		ChatRoom chatRoom = findChatRoomById(roomId);
		chatRoom.exitBy(userId); // 둘 다 나가면 isActive=false로 전환됨
	}

	private ChatRoom findChatRoomById(Long roomId) {
		return chatRoomRepository.findById(roomId)
				       .orElseThrow(() -> new IllegalStateException("채팅방이 존재하지 않습니다."));
	}

	public void markActiveIfInactive(Long roomId) {
		ChatRoom chatRoom = findChatRoomById(roomId);
		if (!chatRoom.isActive()) {
			chatRoom.activate();
		}
	}

	@Transactional(readOnly = true)
	public boolean isMember(Long roomId, Long userId) {
		return chatRoomRepository.existsByIdAndParticipantId(roomId, userId);
	}

	public void returnBy(Long roomId, Long userId) {
		ChatRoom chatRoom = findChatRoomById(roomId);
		chatRoom.validateParticipant(userId);

		if (chatRoom.isArchived()) {
			throw new IllegalStateException("종료된 대화방입니다. 새 대화방을 사용하세요.");
		}

		chatRoom.returnBy(userId);
		if (!chatRoom.isActive()) {
			chatRoom.activate();    // 새 대화 시작 시 활성화
		}
	}
}
