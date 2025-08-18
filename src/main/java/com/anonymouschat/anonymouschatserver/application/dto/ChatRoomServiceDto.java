package com.anonymouschat.anonymouschatserver.application.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public class ChatRoomServiceDto {
	public record SummaryResult(
			Long roomId,
			Long opponentId,
			String opponentNickname,
			Integer opponentAge,
			String opponentRegion,
			String opponentProfileImageUrl,
			LocalDateTime lastMessageTime
	) {
		@QueryProjection
		public SummaryResult {}
	}
}
