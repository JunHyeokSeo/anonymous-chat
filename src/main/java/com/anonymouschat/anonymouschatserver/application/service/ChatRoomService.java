package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomServiceDto;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.ConflictException;
import com.anonymouschat.anonymouschatserver.common.exception.InternalServerException;
import com.anonymouschat.anonymouschatserver.common.exception.NotFoundException;
import com.anonymouschat.anonymouschatserver.common.log.LogTag;
import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {
	private final ChatRoomRepository chatRoomRepository;

	public ChatRoom createOrFind(User initiator, User recipient) {
		long a = initiator.getId(), b = recipient.getId();
		long left = Math.min(a, b), right = Math.max(a, b);

		Optional<ChatRoom> existing = chatRoomRepository.findActiveByPair(left, right);
		if (existing.isPresent()) {
			ChatRoom chatRoom = existing.get();
			log.info("{}기존 활성 채팅방 반환 - roomId={}, initiatorId={}, recipientId={}",
					LogTag.CHAT, chatRoom.getId(), initiator.getId(), recipient.getId());
			chatRoom.returnBy(initiator.getId());
			return chatRoom;
		}

		try {
			ChatRoom newRoom = chatRoomRepository.save(new ChatRoom(initiator, recipient));
			log.info("{}신규 채팅방 생성 - roomId={}, initiatorId={}, recipientId={}",
					LogTag.CHAT, newRoom.getId(), initiator.getId(), recipient.getId());
			return newRoom;
		} catch (DataIntegrityViolationException e) {
			log.warn("{}채팅방 생성 중 동시성 충돌 - initiatorId={}, recipientId={}",
					LogTag.CHAT, initiator.getId(), recipient.getId());
			return chatRoomRepository.findActiveByPair(left, right)
					       .orElseThrow(() -> new InternalServerException(ErrorCode.CHAT_ROOM_CONCURRENCY_ERROR));
		}
	}

	public ChatRoom getVerifiedChatRoomOrThrow(Long userId, Long roomId) {
		ChatRoom chatRoom = findChatRoomById(roomId);
		chatRoom.validateParticipant(userId);
		return chatRoom;
	}

	public List<ChatRoomServiceDto.Summary> getMyActiveChatRooms(Long userId) {
		return chatRoomRepository.findActiveChatRoomsByUser(userId);
	}

	public void exit(Long userId, Long roomId) {
		ChatRoom chatRoom = findChatRoomById(roomId);
		chatRoom.exitBy(userId);
		log.info("{}채팅방 나가기 처리 완료 - userId={}, roomId={}", LogTag.CHAT, userId, roomId);
	}

	public void markActiveIfInactive(Long roomId) {
		ChatRoom chatRoom = findChatRoomById(roomId);
		if (!chatRoom.isActive()) {
			chatRoom.activate();
			log.info("{}비활성 채팅방 복원 처리 - roomId={}", LogTag.CHAT, roomId);
		}
	}

	public boolean isMember(Long roomId, Long userId) {
		return chatRoomRepository.existsByIdAndParticipantId(roomId, userId);
	}

	public void returnBy(Long roomId, Long userId) {
		ChatRoom chatRoom = findChatRoomById(roomId);

		if (chatRoom.isArchived()) {
			log.warn("{}채팅방 복귀 불가 - 이미 종료된 채팅방 - userId={}, roomId={}", LogTag.CHAT, userId, roomId);
			throw new ConflictException(ErrorCode.CHAT_ROOM_CLOSED);
		}

		chatRoom.returnBy(userId);
		log.info("{}채팅방 복귀 처리 완료 - userId={}, roomId={}", LogTag.CHAT, userId, roomId);
	}

	private ChatRoom findChatRoomById(Long roomId) {
		return chatRoomRepository.findById(roomId)
				       .orElseThrow(() -> new NotFoundException(ErrorCode.CHAT_ROOM_NOT_FOUND));
	}
}

