package com.anonymouschat.anonymouschatserver.web.socket.dto;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.socket.MessageValidationException;
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
		if (roomId == null) throw new MessageValidationException(ErrorCode.ROOM_ID_REQUIRED);
		if (type == null) throw new MessageValidationException(ErrorCode.MESSAGE_TYPE_REQUIRED);

		// 타입별 제약
		switch (type) {
			case CHAT -> {
				if (content == null || content.isBlank())
					throw new MessageValidationException(ErrorCode.CONTENT_REQUIRED_FOR_CHAT);
				if (content.length() > 2000)
					throw new MessageValidationException(ErrorCode.CONTENT_LENGTH_EXCEEDED);
			}
			case READ, ENTER, LEAVE -> {
				// READ/ENTER/LEAVE는 content 금지
				if (content != null && !content.isBlank())
					throw new MessageValidationException(ErrorCode.CONTENT_SHOULD_BE_EMPTY);
			}
		}
	}
}