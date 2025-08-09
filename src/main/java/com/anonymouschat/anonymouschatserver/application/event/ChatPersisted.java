package com.anonymouschat.anonymouschatserver.application.event;

import lombok.Builder;

@Builder
public record ChatPersisted(
		Long roomId,
		Long senderId,
		Long messageId,
		String content,
		java.time.Instant createdAt
) {}
