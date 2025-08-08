package com.anonymouschat.anonymouschatserver.web.socket.dto;

public record ChatInboundMessage(
		Long roomId,
		MessageType type,
		String content
) {}
