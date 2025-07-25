package com.anonymouschat.anonymouschatserver.application.dto;

public record MarkMessagesAsReadCommand(
		Long chatRoomId,
		Long userId
) {}
