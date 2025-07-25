package com.anonymouschat.anonymouschatserver.application.dto;

import java.time.LocalDateTime;

public record MessageResult(
		Long messageId,
		Long chatRoomId,
		Long senderId,
		String content,
		boolean isRead,
		LocalDateTime sentAt,
		boolean isMine
) {}
