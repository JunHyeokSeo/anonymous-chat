package com.anonymouschat.anonymouschatserver.application.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

import java.time.LocalDateTime;

public class ChatRoomServiceDto {
	@Builder
	public record Summary(
			Long chatRoomId,
			Long opponentId,
			String opponentNickname,
			Integer opponentAge,
			String opponentRegion,
			String opponentProfileImageUrl,
			LocalDateTime lastMessageTime
	) {
		@QueryProjection
		public Summary(
				Long chatRoomId,
				Long opponentId,
				String opponentNickname,
				Integer opponentAge,
				String opponentRegion,
				String opponentProfileImageUrl,
				LocalDateTime lastMessageTime
		) {
			this.chatRoomId = chatRoomId;
			this.opponentId = opponentId;
			this.opponentNickname = opponentNickname;
			this.opponentAge = opponentAge;
			this.opponentRegion = opponentRegion;
			this.opponentProfileImageUrl = opponentProfileImageUrl;
			this.lastMessageTime = lastMessageTime;
		}
	}
}
