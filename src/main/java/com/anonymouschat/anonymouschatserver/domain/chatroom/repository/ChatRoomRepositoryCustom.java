package com.anonymouschat.anonymouschatserver.domain.chatroom.repository;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomSummaryResult;
import com.anonymouschat.anonymouschatserver.domain.chatroom.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.chatroom.type.ChatRoomStatus;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepositoryCustom {
	List<ChatRoomSummaryResult> findActiveChatRoomsByUser(Long userId, ChatRoomStatus status);
	Optional<ChatRoom> findByParticipantsAndStatus(Long userId1, Long userId2, ChatRoomStatus status);
}
