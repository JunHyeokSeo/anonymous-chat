package com.anonymouschat.anonymouschatserver.application.event;

import lombok.Builder;

@Builder
public record ChatMessageSaveEvent(
		Long senderId,
		Long chatRoomId,
		String content
) {}
