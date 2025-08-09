package com.anonymouschat.anonymouschatserver.application.event;

import lombok.Builder;

@Builder
public record ChatSave(
		Long senderId,
		Long roomId,
		String content
) {}
