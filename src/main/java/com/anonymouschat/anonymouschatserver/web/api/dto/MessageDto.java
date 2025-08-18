package com.anonymouschat.anonymouschatserver.web.api.dto;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class MessageDto {

	public record SendMessageRequest(
			@NotNull(message = "채팅방 ID는 필수입니다.")
			Long roomId,

			@NotBlank(message = "메시지 내용은 비어 있을 수 없습니다.")
			String content
	) {
		public static MessageUseCaseDto.SendMessageRequest from(SendMessageRequest request, Long senderId) {
			return new MessageUseCaseDto.SendMessageRequest(
					request.roomId(),
					senderId,
					request.content()
			);
		}
	}

	public record SendMessageResponse(
			Long messageId
	) {
		public static SendMessageResponse from(Long messageId) {
			return new SendMessageResponse(messageId);
		}
	}

	public record GetMessagesRequest(
			@NotNull(message = "채팅방 ID는 필수입니다.")
			Long roomId,

			Long lastMessageId,

			@Min(value = 1, message = "최소 1개 이상 요청해야 합니다.")
			int limit
	) {
		public static MessageUseCaseDto.GetMessagesRequest from(GetMessagesRequest request, Long userId) {
			return new MessageUseCaseDto.GetMessagesRequest(
					request.roomId(),
					userId,
					request.lastMessageId(),
					request.limit()
			);
		}
	}

	public record MessageResponse(
			Long messageId,
			Long roomId,
			Long senderId,
			String content,
			LocalDateTime sentAt,
			boolean isMine
	) {
		public static MessageResponse from(MessageUseCaseDto.MessageResponse response) {
			return new MessageResponse(
					response.messageId(),
					response.roomId(),
					response.senderId(),
					response.content(),
					response.sentAt(),
					response.isMine()
			);
		}
	}

	public record MarkMessagesAsReadRequest(
			@NotNull(message = "채팅방 ID는 필수입니다.")
			Long roomId
	) {
		public static MessageUseCaseDto.MarkMessagesAsReadRequest from(MarkMessagesAsReadRequest request, Long userId) {
			return new MessageUseCaseDto.MarkMessagesAsReadRequest(
					request.roomId(),
					userId
			);
		}
	}

	public record GetLastReadMessageRequest(
			@NotNull(message = "채팅방 ID는 필수입니다.")
			Long roomId
	) {
		public static MessageUseCaseDto.GetLastReadMessageRequest from(GetLastReadMessageRequest request, Long userId) {
			return new MessageUseCaseDto.GetLastReadMessageRequest(
					request.roomId(),
					userId
			);
		}
	}
}
