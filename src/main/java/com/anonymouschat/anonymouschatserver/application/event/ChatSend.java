package com.anonymouschat.anonymouschatserver.application.event;

import lombok.Builder;

@Builder
public record ChatSend(
		Long senderId,
		Long roomId,
		String content
) {}
