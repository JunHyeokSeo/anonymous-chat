package com.anonymouschat.anonymouschatserver.common.socket.dto;

import com.anonymouschat.anonymouschatserver.common.enums.MessageType;
import lombok.Builder;

import java.time.Instant;

@Builder
public record ChatOutboundMessage(
		Long roomId,
		MessageType type,
		Long senderId,
		String senderNickname,
		String content,
		Instant timestamp
) {}
