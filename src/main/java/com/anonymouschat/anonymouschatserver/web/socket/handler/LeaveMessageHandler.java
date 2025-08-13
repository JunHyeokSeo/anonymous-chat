package com.anonymouschat.anonymouschatserver.web.socket.handler;

import com.anonymouschat.anonymouschatserver.web.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.web.socket.support.WsLogTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static com.anonymouschat.anonymouschatserver.web.socket.support.WebSocketUtil.extractUserId;

/**
 * {@link MessageType#LEAVE} 타입의 인바운드 메시지를 처리하는 핸들러입니다.
 * 사용자를 채팅방 참여자 목록에서 제거하는 역할을 수행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveMessageHandler implements MessageHandler {
	private final ChatSessionManager sessionManager;

	/**
	 * 이 핸들러가 처리할 메시지 타입을 반환합니다.
	 *
	 * @return {@link MessageType#LEAVE}
	 */	@Override
	public MessageType type() { return MessageType.LEAVE; }

	/**
	 * 수신된 퇴장 메시지를 처리합니다.
	 * 사용자를 채팅방 참여자 목록에서 제거하고, 성공 여부에 따라 로그를 남깁니다.
	 *
	 * @param session 메시지를 보낸 WebSocket 세션
	 * @param inbound 처리할 인바운드 메시지 객체
	 */	@Override
	public void handle(WebSocketSession session, ChatInboundMessage inbound) {
		Long userId = extractUserId(session);
		Long roomId = inbound.roomId();

		// 방 참여자 목록에서 제거
		boolean removed = sessionManager.leaveRoom(roomId, userId);
		if (removed)
			log.info("{}userId={} roomId={}", WsLogTag.leave(), userId, roomId);
		else
			log.debug("{}no-op (not a participant) userId={} roomId={}", WsLogTag.leave(), userId, roomId);
	}
}
