package com.anonymouschat.anonymouschatserver.common.socket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * WebSocket 연결된 사용자들의 세션 및 채팅방 참여자를 관리하는 컴포넌트입니다.
 * userId를 기준으로 식별하며, nickname은 표시용 정보로만 사용됩니다.
 */
@Component
@Slf4j
public class ChatSessionManager {
	// 사용자 ID → WebSocket 세션
	private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

	// 채팅방 ID → 참여 중인 userId 목록
	private final Map<Long, Set<Long>> roomParticipants = new ConcurrentHashMap<>();

	/**
	 * 연결된 사용자를 등록합니다.
	 *
	 * @param userId 사용자 ID
	 * @param session WebSocket 세션
	 */
	public void registerSession(Long userId, WebSocketSession session) {
		userSessions.put(userId, session);
	}

	/**
	 * 사용자의 세션을 제거하고, 모든 채팅방에서 해당 사용자를 제거합니다.
	 *
	 * @param userId 사용자 ID
	 */
	public void unregisterSession(Long userId) {
		WebSocketSession session = userSessions.remove(userId);
		if (session != null && session.isOpen()) {
			try {
				session.close();
			} catch (Exception e) {
				log.warn("WebSocket 세션 종료 중 예외 발생 (userId={}): {}", userId, e.getMessage(), e);
			}
		}

		// 모든 채팅방에서 사용자 제거
		roomParticipants.values().forEach(set -> set.remove(userId));
	}

	/**
	 * 특정 채팅방에 사용자를 참여자로 등록합니다.
	 *
	 * @param roomId 채팅방 ID
	 * @param userId 사용자 ID
	 */
	public void joinRoom(Long roomId, Long userId) {
		roomParticipants
				.computeIfAbsent(roomId, id -> new CopyOnWriteArraySet<>())
				.add(userId);
	}

	/**
	 * 특정 채팅방에서 사용자를 제거합니다.
	 * 마지막 남은 참여자 제거 시 room도 정리합니다.
	 *
	 * @param roomId 채팅방 ID
	 * @param userId 사용자 ID
	 */
	public void leaveRoom(Long roomId, Long userId) {
		Set<Long> participants = roomParticipants.get(roomId);
		if (participants != null) {
			participants.remove(userId);
			if (participants.isEmpty()) {
				roomParticipants.remove(roomId);
			}
		}
	}

	/**
	 * 채팅방의 모든 참여자 userId 목록을 반환합니다.
	 *
	 * @param roomId 채팅방 ID
	 * @return 참여자 userId 집합
	 */
	public Set<Long> getParticipants(Long roomId) {
		return roomParticipants.getOrDefault(roomId, Set.of());
	}

	/**
	 * 특정 사용자의 WebSocket 세션을 반환합니다.
	 *
	 * @param userId 사용자 ID
	 * @return WebSocket 세션, 없으면 null
	 */
	public WebSocketSession getSession(Long userId) {
		return userSessions.get(userId);
	}

	/**
	 * 현재 등록된 모든 세션을 반환한다.
	 * 하트비트 등 전체 순회 시 사용
	 */
	public Map<Long, WebSocketSession> getAllSessions() {
		return userSessions;
	}

	/**
	 * 현재 사용자가 참여중인 모든 채팅방 반환
	 * afterConnectionClosed 등 소켓 연결 종료 시 사용.
	 */
	public Set<Long> getJoinedRoomsByUser(Long userId) {
		return roomParticipants.entrySet().stream()
				       .filter(entry -> entry.getValue().contains(userId))
				       .map(Map.Entry::getKey)
				       .collect(Collectors.toSet());
	}
}
