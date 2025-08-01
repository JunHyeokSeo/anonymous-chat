package com.anonymouschat.anonymouschatserver.application.dto;

import lombok.Builder;

import java.time.LocalDateTime;

public class ChatRoomUseCaseDto {

	@Builder
	public record Summary(
			Long chatRoomId,
			Long opponentId,
			String opponentNickname,
			int opponentAge,
			String opponentRegion,
			String opponentProfileImageUrl,
			LocalDateTime lastMessageTime
	) {
		public static Summary from(ChatRoomServiceDto.Summary result) {
			return Summary.builder()
					       .chatRoomId(result.chatRoomId())
					       .opponentId(result.opponentId())
					       .opponentNickname(result.opponentNickname())
					       .opponentAge(result.opponentAge())
					       .opponentRegion(result.opponentRegion())
					       .opponentProfileImageUrl(result.opponentProfileImageUrl())
					       .lastMessageTime(result.lastMessageTime())
					       .build();
		}
	}
}
