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

/**
 * WebSocket 세션 및 채팅방 참여자 상태를 관리하는 컴포넌트입니다.
 * 사용자 세션 등록/교체, 강제 연결 해제, 채팅방 참여/이탈, 세션 활동 시간 관리 등
 * 실시간 통신에 필요한 핵심적인 세션 및 상태 관리 기능을 제공합니다.
 */
@Slf4j
@Component
public class ChatSessionManager {
	private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
	private final Map<Long, Set<Long>> roomParticipants = new ConcurrentHashMap<>();
	private final Map<Long, Instant> lastActiveAtMap = new ConcurrentHashMap<>();

	/**
	 * 사용자 ID와 새로운 WebSocket 세션을 등록하거나, 기존 세션이 있다면 종료 후 교체합니다.
	 * 세션 등록 시 해당 사용자의 마지막 활동 시간을 현재 시간으로 초기화합니다.
	 *
	 * @param userId 세션을 등록할 사용자 ID
	 * @param newSession 새로 등록할 WebSocket 세션
	 */	public void registerOrReplaceSession(Long userId, WebSocketSession newSession) {
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

	/**
	 * 특정 사용자 ID에 해당하는 WebSocket 세션을 강제로 연결 해제합니다.
	 * 해당 사용자의 세션 정보, 마지막 활동 시간, 참여 중인 모든 채팅방에서 제거합니다.
	 *
	 * @param userId 연결 해제할 사용자 ID
	 * @param status 연결 해제 상태 코드
	 */	public void forceDisconnect(Long userId, CloseStatus status) {
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

	/**
	 * 특정 WebSocket 세션을 강제로 연결 해제합니다.
	 * 세션에서 사용자 ID를 추출할 수 없는 경우(예: 인증 정보 없음) 세션만 종료하고 매핑은 정리하지 않습니다.
	 * 사용자 ID를 추출할 수 있다면 {@link #forceDisconnect(Long, CloseStatus)}를 호출하여 통합 처리합니다.
	 *
	 * @param session 연결 해제할 WebSocket 세션
	 * @param status 연결 해제 상태 코드
	 */	public void forceDisconnect(WebSocketSession session, CloseStatus status) {
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


	/**
	 * 사용자를 특정 채팅방에 참여시킵니다.
	 * 채팅방이 존재하지 않으면 새로 생성하고, 사용자를 해당 방의 참여자 목록에 추가합니다.
	 *
	 * @param roomId 참여할 채팅방 ID
	 * @param userId 참여할 사용자 ID
	 */	public void joinRoom(Long roomId, Long userId) {
		roomParticipants.computeIfAbsent(roomId, id -> new CopyOnWriteArraySet<>()).add(userId);
	}

	/**
	 * 사용자를 특정 채팅방에서 이탈시킵니다.
	 * 해당 사용자가 마지막 참여자일 경우 채팅방 정보도 정리합니다.
	 *
	 * @param roomId 이탈할 채팅방 ID
	 * @param userId 이탈할 사용자 ID
	 * @return 사용자가 성공적으로 제거되었는지 여부
	 */	public boolean leaveRoom(Long roomId, Long userId) {
		Set<Long> set = roomParticipants.get(roomId);
		if (set == null) return false;
		boolean removed = set.remove(userId);
		if (set.isEmpty()) roomParticipants.remove(roomId);
		return removed;
	}

	/**
	 * 특정 채팅방의 현재 참여자 목록을 반환합니다.
	 * 반환된 Set은 불변(unmodifiable) 뷰이므로 외부에서 수정할 수 없습니다.
	 *
	 * @param roomId 참여자 목록을 조회할 채팅방 ID
	 * @return 해당 채팅방의 참여자 ID Set (참여자가 없으면 빈 Set 반환)
	 */	public Set<Long> getParticipants(Long roomId) {
		return Collections.unmodifiableSet(roomParticipants.getOrDefault(roomId, Set.of()));
	}

	/**
	 * 특정 사용자 ID에 해당하는 WebSocket 세션을 반환합니다.
	 *
	 * @param userId 조회할 사용자 ID
	 * @return 해당 사용자의 WebSocket 세션 (없으면 null)
	 */	public WebSocketSession getSession(Long userId) {
		return userSessions.get(userId);
	}

	/**
	 * 현재 등록된 모든 사용자 세션의 맵을 반환합니다.
	 * 반환된 Map은 불변(unmodifiable) 뷰이므로 외부에서 수정할 수 없습니다.
	 *
	 * @return 사용자 ID를 키로, WebSocketSession을 값으로 하는 Map
	 */	public Map<Long, WebSocketSession> getAllSessions() {
		return Collections.unmodifiableMap(userSessions);
	}

	/**
	 * 특정 사용자의 마지막 활동 시간을 현재 시간으로 갱신합니다.
	 *
	 * @param userId 활동 시간을 갱신할 사용자 ID
	 */	public void updateLastActiveAt(Long userId) {
		lastActiveAtMap.put(userId, Instant.now());
	}

	/**
	 * 특정 사용자의 마지막 활동 시간을 반환합니다.
	 * 활동 기록이 없으면 Epoch 시간을 반환합니다.
	 *
	 * @param userId 조회할 사용자 ID
	 * @return 마지막 활동 시간 (Instant 객체)
	 */	public Instant getLastActiveAt(Long userId) {
		return lastActiveAtMap.getOrDefault(userId, Instant.EPOCH);
	}

	/**
	 * 내부 전용: 특정 사용자 ID에 해당하는 세션, 마지막 활동 시간, 모든 채팅방 참여 정보를 정리합니다.
	 * 주로 세션 강제 종료 시 호출됩니다.
	 *
	 * @param userId 정리할 사용자 ID
	 */	private void unregisterSession(Long userId) {
		userSessions.remove(userId);
		lastActiveAtMap.remove(userId);
		roomParticipants.values().forEach(ps -> ps.remove(userId));
		log.debug("[WS] session/state cleared: userId={}", userId);
	}

	/**
	 * WebSocketSession의 ID를 안전하게 반환합니다. 예외 발생 시 "unknown"을 반환합니다.
	 *
	 * @param s WebSocketSession 객체
	 * @return 세션 ID 또는 "unknown"
	 */	private String safeId(WebSocketSession s) {
		try { return s.getId(); } catch (Exception ignored) { return "unknown"; }
	}

	/**
	 * 특정 사용자가 특정 채팅방의 참여자인지 확인합니다.
	 *
	 * @param roomId 확인할 채팅방 ID
	 * @param userId 확인할 사용자 ID
	 * @return 참여자 여부
	 */	public boolean isParticipant(Long roomId, Long userId) {
		return getParticipants(roomId).contains(userId);
	}
}
