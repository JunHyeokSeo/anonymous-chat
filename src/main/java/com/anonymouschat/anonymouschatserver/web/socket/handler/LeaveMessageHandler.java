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

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveMessageHandler implements MessageHandler {
	private final ChatSessionManager sessionManager;

	@Override
	public MessageType type() { return MessageType.LEAVE; }

	@Override
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
