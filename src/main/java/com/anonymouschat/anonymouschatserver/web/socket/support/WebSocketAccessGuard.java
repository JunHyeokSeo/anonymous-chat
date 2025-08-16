package com.anonymouschat.anonymouschatserver.web.socket.support;

import com.anonymouschat.anonymouschatserver.application.service.ChatRoomService;
import com.anonymouschat.anonymouschatserver.common.log.LogTag;
import com.anonymouschat.anonymouschatserver.web.socket.ChatSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket 연결 및 메시지 처리에 대한 접근 제어(인가)를 담당하는 가드 클래스입니다.
 * 사용자의 채팅방 멤버십 및 참여 여부를 확인하여 비정상적인 접근을 차단합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAccessGuard {

	private final ChatRoomService chatRoomService;
	private final ChatSessionManager sessionManager;

	/**
	 * 사용자가 채팅방에 입장(ENTER)할 수 있는지 확인합니다.
	 * 데이터베이스를 통해 사용자가 해당 채팅방의 멤버인지 검증하며, 멤버가 아닐 경우 세션을 종료합니다.
	 *
	 * @param session 현재 WebSocket 세션
	 * @param roomId 입장하려는 채팅방 ID
	 * @param userId 사용자 ID
	 * @return 입장 허용 여부 (true: 허용, false: 거부 및 세션 종료)
	 */
	public boolean ensureEnterAllowed(WebSocketSession session, Long roomId, Long userId) {
		boolean member = chatRoomService.isMember(roomId, userId);
		if (!member) {
			log.warn("{}ENTER denied userId={} roomId={}", LogTag.WS_POLICY, userId, roomId);
			sessionManager.forceDisconnect(session, CloseStatus.POLICY_VIOLATION);
			return false;
		}
		return true;
	}

	/**
	 * 사용자가 채팅, 읽음 처리, 퇴장 등 특정 액션(CHAT/READ/LEAVE)을 수행할 수 있는지 확인합니다.
	 * 메모리 내의 현재 참여자 목록을 통해 사용자가 해당 채팅방에 참여 중인지 검증하며, 참여 중이 아닐 경우 세션을 종료합니다.
	 *
	 * @param session 현재 WebSocket 세션
	 * @param roomId 액션을 수행하려는 채팅방 ID
	 * @param userId 사용자 ID
	 * @return 액션 허용 여부 (true: 허용, false: 거부 및 세션 종료)
	 */
	public boolean ensureParticipant(WebSocketSession session, Long roomId, Long userId) {
		boolean ok = sessionManager.isParticipant(roomId, userId);
		if (!ok) {
			log.warn("{}Not a participant userId={} roomId={} -> closing BAD_DATA", LogTag.WS_POLICY, userId, roomId);
			sessionManager.forceDisconnect(session, CloseStatus.BAD_DATA);
			return false;
		}
		return true;
	}
}
