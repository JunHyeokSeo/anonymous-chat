package com.anonymouschat.anonymouschatserver.web.socket.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = false) // 선언 안 된 필드 오면 역직렬화 실패
public record ChatInboundMessage(
		Long roomId,
		MessageType type,
		String content
) {
	public ChatInboundMessage {
		if (roomId == null) throw new IllegalArgumentException("roomId is required");
		if (type == null) throw new IllegalArgumentException("type is required");

		// 타입별 제약
		switch (type) {
			case CHAT -> {
				if (content == null || content.isBlank())
					throw new IllegalArgumentException("content is required for CHAT");
				if (content.length() > 2000)
					throw new IllegalArgumentException("content length exceeds limit");
			}
			case READ, ENTER, LEAVE -> {
				// READ/ENTER/LEAVE는 content 금지
				if (content != null && !content.isBlank())
					throw new IllegalArgumentException("content must be empty for " + type);
			}
		}
	}
}
