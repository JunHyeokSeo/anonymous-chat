package com.anonymouschat.anonymouschatserver.common.socket.dto;

public record ChatInboundMessage(
		Long roomId,
		MessageType type,
		String content
) {}
