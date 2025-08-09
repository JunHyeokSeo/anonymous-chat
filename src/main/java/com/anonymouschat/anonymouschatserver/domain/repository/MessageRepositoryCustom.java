package com.anonymouschat.anonymouschatserver.domain.repository;

import com.anonymouschat.anonymouschatserver.domain.entity.Message;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepositoryCustom {
	List<Message> findMessagesAfterExitTimeWithCursor(Long roomId, LocalDateTime lastExitedAt, Long lastMessageId, int limit);
	Long updateMessagesAsRead(Long roomId, Long userId);
	Long findMaxReadMessageId(Long roomId, Long senderId);
}
