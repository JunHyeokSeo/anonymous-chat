package com.anonymouschat.anonymouschatserver.domain.repository;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomServiceDto;
import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.type.ChatRoomStatus;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepositoryCustom {
	List<ChatRoomServiceDto.Summary> findActiveChatRoomsByUser(Long userId, ChatRoomStatus status);
	Optional<ChatRoom> findByParticipantsAndStatus(Long userId1, Long userId2, ChatRoomStatus status);
}
