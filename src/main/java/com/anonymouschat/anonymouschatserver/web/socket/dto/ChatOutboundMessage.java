package com.anonymouschat.anonymouschatserver.web.socket.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ChatOutboundMessage(
		Long roomId,
		MessageType type,
		Long senderId,
		String content,
		Instant timestamp,
        Long lastReadMessageId // nullable: READ 타입일 때만 사용.
) { }
