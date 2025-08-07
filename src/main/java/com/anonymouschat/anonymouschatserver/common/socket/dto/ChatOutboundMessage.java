package com.anonymouschat.anonymouschatserver.common.socket.dto;

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
) {
	public static ChatOutboundMessage systemMessage(Long roomId, Long senderId, String content) {
		return ChatOutboundMessage.builder()
				       .roomId(roomId)
				       .type(MessageType.MESSAGE) // 시스템 메시지도 일반 메시지 타입으로 보냄
				       .senderId(senderId)
				       .senderNickname("[시스템]")
				       .content(content)
				       .timestamp(Instant.now())
				       .build();
	}
}
