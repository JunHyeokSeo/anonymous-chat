package com.anonymouschat.anonymouschatserver.web.socket.handler;

import com.anonymouschat.anonymouschatserver.common.log.LogTag;
import com.anonymouschat.anonymouschatserver.web.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.web.socket.support.WebSocketAccessGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static com.anonymouschat.anonymouschatserver.web.socket.support.WebSocketUtil.extractUserId;

/**
 * {@link MessageType#ENTER} 타입의 인바운드 메시지를 처리하는 핸들러입니다.
 * 사용자가 채팅방에 입장할 수 있는지 권한을 확인하고, 세션 관리자에 사용자 정보를 등록합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnterMessageHandler implements MessageHandler {
	private final ChatSessionManager sessionManager;
	private final WebSocketAccessGuard guard;

	/**
	 * 이 핸들러가 처리할 메시지 타입을 반환합니다.
	 *
	 * @return {@link MessageType#ENTER}
	 */	@Override
	public MessageType type() {
		return MessageType.ENTER;
	}

	/**
	 * 수신된 입장 메시지를 처리합니다.
	 * 1. 사용자가 채팅방에 입장할 권한이 있는지 {@link WebSocketAccessGuard}를 통해 확인합니다.
	 * 2. 권한이 있다면 {@link ChatSessionManager}에 사용자를 채팅방 참여자로 등록합니다.
	 *
	 * @param session 메시지를 보낸 WebSocket 세션
	 * @param inbound 처리할 인바운드 메시지 객체
	 */	@Override
	public void handle(WebSocketSession session, ChatInboundMessage inbound) {
		Long userId = extractUserId(session);
		Long roomId = inbound.roomId();

		if (!guard.ensureEnterAllowed(session, roomId, userId)) return;

		sessionManager.joinRoom(roomId, userId);
		log.info("{}userId={} roomId={}", LogTag.WS_ENTER, userId, roomId);
	}
}
