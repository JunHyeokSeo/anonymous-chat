package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomServiceDto;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.ConflictException;
import com.anonymouschat.anonymouschatserver.common.exception.InternalServerException;
import com.anonymouschat.anonymouschatserver.common.exception.NotFoundException;
import com.anonymouschat.anonymouschatserver.common.log.LogTag;
import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.repository.ChatRoomRepository;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {
	private final ChatRoomRepository chatRoomRepository;

	public ChatRoom createOrFind(User initiator, User recipient) {
		long a = initiator.getId(), b = recipient.getId();
		long left = Math.min(a, b), right = Math.max(a, b);

		// 1) 활성 방 조회
		Optional<ChatRoom> existing = chatRoomRepository.findActiveByPair(left, right);
		if (existing.isPresent()) {
            ChatRoom chatRoom = existing.get();
            log.info("{}Active chat room exists. roomId={}", LogTag.CHAT, chatRoom.getId());
			chatRoom.returnBy(initiator.getId());
			return chatRoom;
		}

		// 2) 없으면 생성 (경합 시 유니크 충돌 → 재조회)
		try {
			return chatRoomRepository.save(new ChatRoom(initiator, recipient));
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn("{}Concurrency issue on creating chat room. initiatorId={}, recipientId={}", LogTag.CHAT, initiator.getId(), recipient.getId());
			return chatRoomRepository.findActiveByPair(left, right)
					       .orElseThrow(() -> new InternalServerException(ErrorCode.CHAT_ROOM_CONCURRENCY_ERROR));
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
        log.info("{}userId={}, roomId={}", LogTag.CHAT, userId, roomId);
		ChatRoom chatRoom = findChatRoomById(roomId);
		chatRoom.exitBy(userId); // 둘 다 나가면 isActive=false로 전환됨
	}

	private ChatRoom findChatRoomById(Long roomId) {
		return chatRoomRepository.findById(roomId)
				       .orElseThrow(() -> new NotFoundException(ErrorCode.CHAT_ROOM_NOT_FOUND));
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
        log.info("{}userId={}, roomId={}", LogTag.CHAT, userId, roomId);
		ChatRoom chatRoom = findChatRoomById(roomId);

		if (chatRoom.isArchived()) {
			throw new ConflictException(ErrorCode.CHAT_ROOM_CLOSED);
		}

		chatRoom.returnBy(userId);
	}
}