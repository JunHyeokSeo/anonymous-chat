package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.entity.Message;
import com.anonymouschat.anonymouschatserver.domain.repository.MessageRepository;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MessageService {

	private final MessageRepository messageRepository;

	public Message saveMessage(ChatRoom chatRoom, User sender, String content) {
		Message message = Message.builder()
				                .chatRoom(chatRoom)
				                .sender(sender)
				                .content(content)
				                .build();
		return messageRepository.save(message);
	}

	@Transactional(readOnly = true)
	public List<Message> getMessages(ChatRoom chatRoom, LocalDateTime lastExitedAt, Long lastMessageId, int limit) {
		return messageRepository.findMessagesAfterExitTimeWithCursor(chatRoom.getId(), lastExitedAt, lastMessageId, limit);
	}

	// TODO: 추후 대량 메시지 처리 성능 개선을 위해 ReadTracking 구조 도입 고려
	public Long markMessagesAsRead(Long chatRoomId, Long userId) {
		return messageRepository.updateMessagesAsRead(chatRoomId, userId);
	}

	public Long findLastReadMessageIdByReceiver(Long chatRoomId, Long senderId) {
		return messageRepository.findMaxReadMessageId(chatRoomId, senderId);
	}
}

