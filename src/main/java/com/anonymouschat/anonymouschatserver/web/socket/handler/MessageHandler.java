package com.anonymouschat.anonymouschatserver.web.socket.handler;

import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket 메시지 타입별 처리를 위한 핸들러 인터페이스입니다.
 * 각 메시지 타입에 대해 별도의 구현체를 등록하여 처리 로직을 분리합니다.
 */
public interface MessageHandler {

	/**
	 * 이 핸들러가 처리할 메시지 타입을 반환합니다.
	 *
	 * @return 처리할 {@link MessageType}
	 */	MessageType type();

	/**
	 * 수신된 WebSocket 메시지를 처리하는 로직을 정의합니다.
	 *
	 * @param session 현재 메시지를 보낸 WebSocket 세션
	 * @param inbound 처리할 인바운드 메시지 페이로드 (roomId, content, type 포함)
	 */	void handle(WebSocketSession session, ChatInboundMessage inbound);
}
