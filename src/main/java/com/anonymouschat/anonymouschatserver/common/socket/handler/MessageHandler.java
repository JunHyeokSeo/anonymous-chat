package com.anonymouschat.anonymouschatserver.common.socket.handler;

import com.anonymouschat.anonymouschatserver.common.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.common.socket.dto.MessageType;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket 메시지 타입별 처리 핸들러 인터페이스입니다.
 * 각 메시지 타입에 대해 별도의 구현체를 등록하여 처리합니다.
 */
public interface MessageHandler {

	/**
	 * 이 핸들러가 처리할 메시지 타입
	 */
	MessageType type();

	/**
	 * WebSocket 메시지 처리 로직
	 *
	 * @param session 현재 메시지를 보낸 세션
	 * @param inbound 메시지 페이로드 (roomId, content, type 포함)
	 */
	void handle(WebSocketSession session, ChatInboundMessage inbound);
}
