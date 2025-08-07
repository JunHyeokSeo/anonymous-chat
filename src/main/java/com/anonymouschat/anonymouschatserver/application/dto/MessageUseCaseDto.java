package com.anonymouschat.anonymouschatserver.application.dto;

import com.anonymouschat.anonymouschatserver.domain.entity.Message;
import lombok.Builder;

import java.time.LocalDateTime;
public class MessageUseCaseDto {
	@Builder
	public record MessageResult(
			Long messageId,
			Long chatRoomId,
			Long senderId,
			String content,
			LocalDateTime sentAt,
			boolean isMine
	) {
		public static MessageResult from(Message message, Long userId) {
			boolean isMine = message.isSentBy(userId);
			return MessageResult.builder()
					       .messageId(message.getId())
					       .chatRoomId(message.getChatRoom().getId())
					       .senderId(message.getSender().getId())
					       .content(message.getContent())
					       .sentAt(message.getSentAt())
					       .isMine(isMine)
					       .build();
		}
	}

	@Builder
	public record GetMessages(
			Long chatRoomId,
			Long userId,
			Long lastMessageId, // null 이면 최신부터 시작
			int limit
	) {}

	@Builder
	public record SendMessage(
			Long chatRoomId,
			Long senderId,
			String content
	) {}

	@Builder
	public record MarkMessagesAsRead(
			Long chatRoomId,
			Long userId
	) {}

	@Builder
	public record GetLastReadMessage(
			Long chatRoomId,
			Long userId
	) { }
}
