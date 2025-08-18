package com.anonymouschat.anonymouschatserver.web.api.dto;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomUseCaseDto;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ChatRoomDto {

	public record CreateRequest(
			@NotNull(message = "대화 상대 사용자 ID는 필수입니다.")
			Long recipientId
	) {}

	public record CreateResponse(
			Long roomId
	) {
		public static CreateResponse from(Long roomId) {
			return new CreateResponse(roomId);
		}
	}

	public record SummaryResponse(
			Long roomId,
			Long opponentId,
			String opponentNickname,
			int opponentAge,
			String opponentRegion,
			String opponentProfileImageUrl,
			LocalDateTime lastMessageTime
	) {
		public static SummaryResponse from(ChatRoomUseCaseDto.SummaryResponse response) {
			return new SummaryResponse(
					response.roomId(),
					response.opponentId(),
					response.opponentNickname(),
					response.opponentAge(),
					response.opponentRegion(),
					response.opponentProfileImageUrl(),
					response.lastMessageTime()
			);
		}
	}
}
