package com.anonymouschat.anonymouschatserver.application.dto;

public record SendMessageCommand(
		Long chatRoomId,
		Long senderId,
		String content
) {}
