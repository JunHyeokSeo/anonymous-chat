package com.anonymouschat.anonymouschatserver.web.socket;

import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
public class ChatSessionManager {
	private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
	private final Map<Long, Set<Long>> roomParticipants = new ConcurrentHashMap<>();
	private final Map<Long, Instant> lastActiveAtMap = new ConcurrentHashMap<>();

	public void registerSession(Long userId, WebSocketSession session) {
		userSessions.put(userId, session);
	}

	public void unregisterSession(Long userId) {
		userSessions.remove(userId);
		rmUserFromAllRooms(userId);
		lastActiveAtMap.remove(userId);
		log.debug("세션 및 참여자 정보 제거 완료: userId={}", userId);
	}

	public void joinRoom(Long roomId, Long userId) {
		roomParticipants.computeIfAbsent(roomId, id -> new CopyOnWriteArraySet<>()).add(userId);
	}

	public Set<Long> getParticipants(Long roomId) {
		return roomParticipants.getOrDefault(roomId, Set.of());
	}

	public WebSocketSession getSession(Long userId) {
		return userSessions.get(userId);
	}

	public Map<Long, WebSocketSession> getAllSessions() {
		return userSessions;
	}

	public void updateLastActiveAt(Long userId) {
		lastActiveAtMap.put(userId, Instant.now());
	}

	public Instant getLastActiveAt(Long userId) {
		return lastActiveAtMap.getOrDefault(userId, Instant.EPOCH);
	}

	/**
	 * 기존 세션 강제 종료 및 상태 정리 통합 헬퍼 메서드
	 */
	public void forceDisconnect(Long userId, CloseStatus status) {
		WebSocketSession session = userSessions.get(userId);
		if (session != null && session.isOpen()) {
			try {
				session.close(status);
				log.info("[Force Disconnect] userId={} status={}", userId, status);
			} catch (IOException e) {
				log.warn("세션 종료 실패: userId={} - {}", userId, e.getMessage());
			}
		}
		unregisterSession(userId);
	}

	public void forceDisconnect(WebSocketSession session, CloseStatus status) {
		Object attr = session.getAttributes().get("principal");
		if (attr instanceof CustomPrincipal principal) {
			forceDisconnect(principal.userId(), status);
		} else {
			try {
				if (session.isOpen()) session.close(status);
			} catch (IOException e) {
				log.warn("세션 종료 실패 (비인증 사용자): {}", e.getMessage());
			}
		}
	}

	/**
	 * 내부 전용: 모든 채팅방에서 유저 제거
	 */
	private void rmUserFromAllRooms(Long userId) {
		roomParticipants.values().forEach(participants -> participants.remove(userId));
	}
}
