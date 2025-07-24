package com.anonymouschat.anonymouschatserver.application.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

@QueryProjection
public record ChatRoomSummaryResult(
		Long chatRoomId,
		Long opponentId,
		String opponentNickname,
		Integer opponentAge,
		String opponentRegion,
		String opponentProfileImageUrl,
		LocalDateTime lastMessageTime
) { }
