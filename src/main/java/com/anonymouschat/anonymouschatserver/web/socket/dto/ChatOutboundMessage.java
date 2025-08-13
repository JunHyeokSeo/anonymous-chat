package com.anonymouschat.anonymouschatserver.web.socket.dto;

import lombok.Builder;

import java.time.Instant;

/**
 * 클라이언트에게 전송되는 WebSocket 메시지의 아웃바운드 페이로드 레코드입니다.
 * 다양한 메시지 타입에 따라 필요한 정보를 포함하며, 필수 필드에 대한 유효성 검사를 수행합니다.
 *
 * @param roomId 메시지가 속한 채팅방 ID
 * @param type 메시지 타입 (CHAT, READ, ENTER, LEAVE 등)
 * @param senderId 메시지를 보낸 사용자 ID
 * @param content 메시지 내용 (CHAT 타입에만 해당)
 * @param timestamp 메시지 전송 시간
 * @param lastReadMessageId 읽음 처리된 마지막 메시지 ID (READ 타입에만 사용, nullable)
 */
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
