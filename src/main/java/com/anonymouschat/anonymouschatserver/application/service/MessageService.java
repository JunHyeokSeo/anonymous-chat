package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.entity.Message;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

	private final MessageRepository messageRepository;

	public Message saveMessage(ChatRoom chatRoom, User sender, String content) {
		Message message = Message.builder()
				                  .chatRoom(chatRoom)
				                  .sender(sender)
				                  .content(content)
				                  .build();
		Message saved = messageRepository.save(message);
		log.info("{}메시지 저장 완료 - messageId={}, roomId={}, senderId={}", LogTag.MESSAGE, saved.getId(), chatRoom.getId(), sender.getId());
		return saved;
	}

	public List<Message> getMessages(ChatRoom chatRoom, LocalDateTime lastExitedAt, Long lastMessageId, int limit) {
		return messageRepository.findMessagesAfterExitTimeWithCursor(chatRoom.getId(), lastExitedAt, lastMessageId, limit);
	}

	public Long markMessagesAsRead(Long roomId, Long userId) {
		Long count = messageRepository.updateMessagesAsRead(roomId, userId);
		log.info("{}메시지 읽음 처리 완료 - roomId={}, userId={}, updatedCount={}", LogTag.MESSAGE, roomId, userId, count);
		return count;
	}

	public Long findLastReadMessageIdByReceiver(Long roomId, Long senderId) {
		return messageRepository.findMaxReadMessageId(roomId, senderId);
	}
}



