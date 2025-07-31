package com.anonymouschat.anonymouschatserver.domain.repository;

import com.anonymouschat.anonymouschatserver.domain.entity.Message;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepositoryCustom {
	List<Message> findMessagesAfterExitTimeWithCursor(Long chatRoomId, LocalDateTime lastExitedAt, Long lastMessageId, int limit);
	void updateMessagesAsRead(Long chatRoomId, Long userId);
}
