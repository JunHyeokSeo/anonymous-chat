package com.anonymouschat.anonymouschatserver.application.dto;

public record GetMessagesCommand(
		Long chatRoomId,
		Long userId,
		Long lastMessageId, // null 이면 최신부터 시작
		int limit
) {}
