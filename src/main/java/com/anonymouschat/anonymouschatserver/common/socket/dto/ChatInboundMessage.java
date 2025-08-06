package com.anonymouschat.anonymouschatserver.common.socket.dto;

import com.anonymouschat.anonymouschatserver.common.enums.MessageType;

public record ChatInboundMessage(
		Long roomId,
		MessageType type,
		String content
) {}
