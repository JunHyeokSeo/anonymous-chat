package com.anonymouschat.anonymouschatserver.web.socket.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 클라이언트로부터 수신되는 WebSocket 메시지의 인바운드 페이로드 레코드입니다.
 * 메시지 타입에 따라 roomId, content 등의 필수 필드 유효성을 검사합니다.
 *
 * @param roomId 메시지가 속한 채팅방 ID
 * @param type 메시지 타입 (CHAT, READ, ENTER, LEAVE 등)
 * @param content 메시지 내용 (CHAT 타입에만 해당, 다른 타입은 null 또는 빈 문자열)
 */
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
