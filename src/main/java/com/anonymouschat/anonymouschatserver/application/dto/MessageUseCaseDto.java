package com.anonymouschat.anonymouschatserver.application.dto;

import com.anonymouschat.anonymouschatserver.domain.entity.Message;
import lombok.Builder;

import java.time.Instant;
import java.time.ZoneOffset;

public class MessageUseCaseDto {
	@Builder
	public record MessageResponse(
			Long messageId,
			Long roomId,
			Long senderId,
			String content,
			Instant sentAt
	) {
		public static MessageResponse from(Message message) {
			return MessageResponse.builder()
					       .messageId(message.getId())
					       .roomId(message.getChatRoom().getId())
					       .senderId(message.getSender().getId())
					       .content(message.getContent())
					       .sentAt(message.getSentAt().toInstant(ZoneOffset.UTC))
					       .build();
		}
	}

	@Builder
	public record GetMessagesRequest(
			Long roomId,
			Long userId,
			Long lastMessageId, // null 이면 최신부터 시작
			int limit
	) {}

	@Builder
	public record SendMessageRequest(
			Long roomId,
			Long senderId,
			String content
	) {}

	@Builder
	public record MarkMessagesAsReadRequest(
			Long roomId,
			Long userId
	) {}

	@Builder
	public record GetLastReadMessageRequest(
			Long roomId,
			Long userId
	) { }
}
