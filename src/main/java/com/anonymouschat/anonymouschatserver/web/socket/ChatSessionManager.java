package com.anonymouschat.anonymouschatserver.web.socket;

import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.web.socket.support.WebSocketUtil;
import com.anonymouschat.anonymouschatserver.web.socket.support.WsLogTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.anonymouschat.anonymouschatserver.web.socket.support.WebSocketUtil.*;

@Slf4j
@Component
public class ChatSessionManager {
	private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
	private final Map<Long, Set<Long>> roomParticipants = new ConcurrentHashMap<>();
	private final Map<Long, Instant> lastActiveAtMap = new ConcurrentHashMap<>();

	/** 기존 세션이 있으면 정상 종료 후 교체 + lastActive 초기화 */
	public void registerOrReplaceSession(Long userId, WebSocketSession newSession) {
		WebSocketSession old = userSessions.put(userId, newSession);
		if (old != null && old.isOpen()) {
			try {
				old.close(CloseStatus.NORMAL);
				log.info("[WS] replaced session: userId={} oldSessionId={}", userId, safeId(old));
			} catch (IOException e) {
				log.warn("[WS] old session close failed: userId={} reason={}", userId, e.getMessage());
			}
		}
		updateLastActiveAt(userId); // 최초 핑/퐁 이전에도 유휴 판정 안 나게 초기화
	}

	/** 외부에서 직접 세션/상태를 지우지 않도록 강제 종료 전용 진입점만 제공 */
	public void forceDisconnect(Long userId, CloseStatus status) {
		WebSocketSession session = userSessions.get(userId);
		if (session != null && session.isOpen()) {
			try {
				session.close(status);
				log.info("[WS] force disconnect: userId={} status={}", userId, status);
			} catch (IOException e) {
				log.warn("[WS] close failed: userId={} reason={}", userId, e.getMessage());
			}
		}
		unregisterSession(userId);
	}

	public void forceDisconnect(WebSocketSession session, CloseStatus status) {
		Long userId = null;

		// 정상 케이스: 세션에서 바로 userId 추출 (예외는 흡수)
		try {
			userId = extractUserId(session);
		} catch (Exception e) {
			// 인증 정보가 없거나 비정상 상태일 수 있음 → 폴백 진행
			log.warn("{}failed to extract userId from sessionId={} reason={}", WsLogTag.err(), safeId(session), e.getMessage());
		}

		// 폴백: 역참조로 userId 찾기
		if (userId == null) {
			for (Map.Entry<Long, WebSocketSession> e : userSessions.entrySet()) {
				if (e.getValue() == session) {
					userId = e.getKey();
					break;
				}
			}
		}

		// userId를 찾았으면 통합 경로로 정리
		if (userId != null) {
			forceDisconnect(userId, status);
			return;
		}

		// session만 직접 종료 (매핑 정리는 없음)
		try {
			if (session.isOpen()) {
				session.close(status);
			}
		} catch (IOException e) {
			log.warn("{}close failed (anonymous) sessionId={} reason={}", WsLogTag.err(), safeId(session), e.getMessage());
		}
	}


	/** 방 참여/이탈 */
	public void joinRoom(Long roomId, Long userId) {
		roomParticipants.computeIfAbsent(roomId, id -> new CopyOnWriteArraySet<>()).add(userId);
	}

	public boolean leaveRoom(Long roomId, Long userId) {
		Set<Long> set = roomParticipants.get(roomId);
		if (set == null) return false;
		boolean removed = set.remove(userId);
		if (set.isEmpty()) roomParticipants.remove(roomId);
		return removed;
	}

	/** 조회 계열은 불변 뷰로 노출 (외부에서 수정 불가) */
	public Set<Long> getParticipants(Long roomId) {
		return Collections.unmodifiableSet(roomParticipants.getOrDefault(roomId, Set.of()));
	}

	public WebSocketSession getSession(Long userId) {
		return userSessions.get(userId);
	}

	public Map<Long, WebSocketSession> getAllSessions() {
		return Collections.unmodifiableMap(userSessions);
	}

	public void updateLastActiveAt(Long userId) {
		lastActiveAtMap.put(userId, Instant.now());
	}

	public Instant getLastActiveAt(Long userId) {
		return lastActiveAtMap.getOrDefault(userId, Instant.EPOCH);
	}

	/** 내부 전용: 세션/참여/활동시간 정리 */
	private void unregisterSession(Long userId) {
		userSessions.remove(userId);
		lastActiveAtMap.remove(userId);
		roomParticipants.values().forEach(ps -> ps.remove(userId));
		log.debug("[WS] session/state cleared: userId={}", userId);
	}

	private String safeId(WebSocketSession s) {
		try { return s.getId(); } catch (Exception ignored) { return "unknown"; }
	}

	public boolean isParticipant(Long roomId, Long userId) {
		return getParticipants(roomId).contains(userId);
	}
}
