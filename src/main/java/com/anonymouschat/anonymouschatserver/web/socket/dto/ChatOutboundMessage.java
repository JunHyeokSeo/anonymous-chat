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
) {
	public ChatOutboundMessage {
		if (roomId == null) throw new IllegalArgumentException("roomId is required");
		if (type == null) throw new IllegalArgumentException("type is required");
		if (senderId == null) throw new IllegalArgumentException("senderId is required");
		if (timestamp == null) throw new IllegalArgumentException("timestamp is required");
		if (type == MessageType.CHAT && (content == null || content.isBlank())) throw new IllegalArgumentException("content is required for CHAT");
	}
}
