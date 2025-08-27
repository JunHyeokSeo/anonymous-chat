package com.anonymouschat.anonymouschatserver.presentation.controller.dto;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class MessageDto {
	@Schema(description = "메시지 조회 요청 DTO")
	public record GetMessagesRequest(
			@Schema(description = "채팅방 ID", example = "1001")
			@NotNull(message = "채팅방 ID는 필수입니다.")
			Long roomId,

			@Schema(description = "마지막 메시지 ID (페이징 커서)", example = "2000")
			Long lastMessageId,

			@Schema(description = "조회할 메시지 개수", example = "20")
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

	@Schema(description = "메시지 단일 응답 DTO")
	public record MessageResponse(
			@Schema(description = "메시지 ID", example = "2001")
			Long messageId,

			@Schema(description = "채팅방 ID", example = "1001")
			Long roomId,

			@Schema(description = "보낸 사용자 ID", example = "3001")
			Long senderId,

			@Schema(description = "메시지 내용", example = "안녕하세요! 반갑습니다 :)")
			String content,

			@Schema(description = "메시지 전송 시각", example = "2025-08-19T13:24:00")
			Instant sentAt
	) {
		public static MessageResponse from(MessageUseCaseDto.MessageResponse response) {
			return new MessageResponse(
					response.messageId(),
					response.roomId(),
					response.senderId(),
					response.content(),
					response.sentAt()
			);
		}
	}

	@Schema(description = "메시지 읽음 처리 요청 DTO")
	public record MarkMessagesAsReadRequest(
			@Schema(description = "채팅방 ID", example = "1001")
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

	@Schema(description = "상대방의 마지막 읽은 메시지 ID 조회 요청 DTO")
	public record GetLastReadMessageRequest(
			@Schema(description = "채팅방 ID", example = "1001")
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
