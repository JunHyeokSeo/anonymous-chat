package com.anonymouschat.anonymouschatserver.web.socket.support;

import com.anonymouschat.anonymouschatserver.application.service.ChatRoomService;
import com.anonymouschat.anonymouschatserver.web.socket.ChatSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAccessGuard {

	private final ChatRoomService chatRoomService;
	private final ChatSessionManager sessionManager;

	/** ENTER 전용: DB 멤버십 검증. 실패 시 즉시 종료. */
	public boolean ensureEnterAllowed(WebSocketSession session, Long roomId, Long userId) {
		boolean member = chatRoomService.isMember(roomId, userId);
		if (!member) {
			log.warn("{}ENTER denied userId={} roomId={}", WsLogTag.policy(), userId, roomId);
			sessionManager.forceDisconnect(session, CloseStatus.POLICY_VIOLATION);
			return false;
		}
		return true;
	}

	/** CHAT/READ/LEAVE 전용: 메모리 참가자 검증. 실패 시 즉시 종료. */
	public boolean ensureParticipant(WebSocketSession session, Long roomId, Long userId) {
		boolean ok = sessionManager.isParticipant(roomId, userId);
		if (!ok) {
			log.warn("{}Not a participant userId={} roomId={} -> closing BAD_DATA", WsLogTag.policy(), userId, roomId);
			sessionManager.forceDisconnect(session, CloseStatus.BAD_DATA);
			return false;
		}
		return true;
	}
}
