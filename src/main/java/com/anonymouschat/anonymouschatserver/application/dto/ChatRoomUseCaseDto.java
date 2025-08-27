package com.anonymouschat.anonymouschatserver.application.dto;

import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.entity.UserProfileImage;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class ChatRoomUseCaseDto {
	@Builder
	public record SummaryResponse(
			Long roomId,
			Long opponentId,
			String opponentNickname,
			int opponentAge,
			String opponentRegion,
			String opponentProfileImageUrl,
			Instant lastMessageTime,
			String lastMessageContent,
			Long unreadCnt
	) {
		public static SummaryResponse from(ChatRoomServiceDto.SummaryResult result) {
			return SummaryResponse.builder()
					       .roomId(result.roomId())
					       .opponentId(result.opponentId())
					       .opponentNickname(result.opponentNickname())
					       .opponentAge(result.opponentAge())
					       .opponentRegion(result.opponentRegion())
					       .opponentProfileImageUrl(result.opponentProfileImageUrl())
					       .lastMessageTime(result.lastMessageTime().toInstant(ZoneOffset.UTC))
					       .lastMessageContent(result.lastMessageContent())
					       .unreadCnt(result.unreadCnt())
					       .build();
		}

		public static SummaryResponse from(Long roomId, LocalDateTime lastMessageTime, User opponent) {
			return SummaryResponse.builder()
					       .roomId(roomId)
					       .opponentId(opponent.getId())
					       .opponentNickname(opponent.getNickname())
					       .opponentAge(opponent.getAge())
					       .opponentRegion(opponent.getRegion().name())
					       .opponentProfileImageUrl(opponent.getProfileImages().stream()
							                                .filter(UserProfileImage::isRepresentative)
							                                .findFirst()
							                                .map(UserProfileImage::getImageUrl)
							                                .orElse(null))
					       .lastMessageTime(lastMessageTime.toInstant(ZoneOffset.UTC))
					       .lastMessageContent("")
					       .unreadCnt(0L)
					       .build();
		}
	}
}
