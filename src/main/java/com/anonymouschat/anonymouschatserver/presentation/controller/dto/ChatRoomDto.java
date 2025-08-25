package com.anonymouschat.anonymouschatserver.presentation.controller.dto;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomUseCaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class ChatRoomDto {

	public record CreateRequest(
			@NotNull(message = "대화 상대 사용자 ID는 필수입니다.")
			@Schema(description = "대화 상대 사용자 ID", example = "42")
			Long recipientId
	) {}

	public record CreateResponse(
			@Schema(description = "생성된 채팅방 ID", example = "1001")
			Long roomId
	) {
		public static CreateResponse from(Long roomId) {
			return new CreateResponse(roomId);
		}
	}

	public record SummaryResponse(
			@Schema(description = "채팅방 ID", example = "1001")
			Long roomId,

			@Schema(description = "상대방 사용자 ID", example = "42")
			Long opponentId,

			@Schema(description = "상대방 닉네임", example = "밍글유저")
			String opponentNickname,

			@Schema(description = "상대방 나이", example = "25")
			int opponentAge,

			@Schema(description = "상대방 지역", example = "서울")
			String opponentRegion,

			@Schema(description = "상대방 프로필 이미지 URL", example = "https://example.com/profile.png")
			String opponentProfileImageUrl,

			@Schema(description = "마지막 메시지 전송 시각", example = "2025-08-19T15:30:00")
			Instant lastMessageTime,

			@Schema(description = "마지막 메시지 내용", example = "안녕하세요!")
			String lastMessageContent,

			@Schema(description = "안 읽은 메시지 수", example = "10")
			Long unreadCount
	) {
		public static SummaryResponse from(ChatRoomUseCaseDto.SummaryResponse response) {
			return new SummaryResponse(
					response.roomId(),
					response.opponentId(),
					response.opponentNickname(),
					response.opponentAge(),
					response.opponentRegion(),
					response.opponentProfileImageUrl(),
					response.lastMessageTime(),
					response.lastMessageContent(),
					response.unreadCnt()
			);
		}
	}
}
