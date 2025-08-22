package com.anonymouschat.anonymouschatserver.presentation.socket.support;

import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 메시지 전송에 대한 레이트 리밋(Rate Limit)을 관리하는 가드 클래스입니다.
 * 사용자별, 메시지 타입별 토큰 버킷(Token Bucket) 알고리즘을 사용하여 메시지 전송 속도를 제어합니다.
 */
@Component
@Slf4j
public class WebSocketRateLimitGuard {

	// 1유저당 타입별 버킷
	private final Map<Long, EnumMap<MessageType, TokenBucket>> buckets = new ConcurrentHashMap<>();

	// 정책: 필요 시 조정
	private static final int CHAT_CAPACITY = 20;          // 최대 토큰
	private static final double CHAT_REFILL_PER_SEC = 20; // 초당 20개 (≈ 20msg/s)

	private static final int READ_CAPACITY = 10;
	private static final double READ_REFILL_PER_SEC = 10; // 초당 10개

	private static final int ENTER_LEAVE_CAPACITY = 5;
	private static final double ENTER_LEAVE_REFILL_PER_SEC = 2; // 초당 2개

	/**
	 * 특정 사용자가 특정 메시지 타입을 전송하는 것을 허용할지 여부를 확인합니다.
	 * 토큰 버킷에서 토큰을 소비하며, 토큰이 없으면 false를 반환합니다.
	 *
	 * @param userId 사용자 ID
	 * @param type 메시지 타입
	 * @return 메시지 전송 허용 여부
	 */
	public boolean allow(long userId, MessageType type) {
		EnumMap<MessageType, TokenBucket> userBuckets =
				buckets.computeIfAbsent(userId, k -> new EnumMap<>(MessageType.class));

		TokenBucket bucket = userBuckets.computeIfAbsent(type, t -> switch (t) {
			case CHAT -> new TokenBucket(CHAT_CAPACITY, CHAT_REFILL_PER_SEC);
			case READ -> new TokenBucket(READ_CAPACITY, READ_REFILL_PER_SEC);
			case ENTER, LEAVE -> new TokenBucket(ENTER_LEAVE_CAPACITY, ENTER_LEAVE_REFILL_PER_SEC);
		});

		boolean allowed = bucket.tryConsume();
		if (!allowed) {
			log.warn("{}Rate limit exceeded. userId={}, type={}", LogTag.WS_POLICY, userId, type);
		}
		return allowed;
	}

	/**
	 * 특정 사용자의 모든 토큰 버킷 정보를 초기화합니다.
	 * 주로 WebSocket 연결 종료 시 메모리 정리를 위해 호출됩니다.
	 *
	 * @param userId 사용자 ID
	 */
	public void clear(long userId) {
		buckets.remove(userId);
	}

	/**
	 * 토큰 버킷 알고리즘을 구현한 내부 클래스입니다.
	 * 스레드 안전하게 토큰을 소비하고 리필합니다.
	 */
	private static final class TokenBucket {
		private final int capacity;
		private final double refillPerSec;

		private double tokens;
		private long lastNanos;

		private TokenBucket(int capacity, double refillPerSec) {
			this.capacity = capacity;
			this.refillPerSec = refillPerSec;
			this.tokens = capacity;
			this.lastNanos = System.nanoTime();
		}

		synchronized boolean tryConsume() {
			refill();
			if (tokens >= 1.0) {
				tokens -= 1.0;
				return true;
			}
			return false;
		}

		private void refill() {
			long now = System.nanoTime();
			double elapsedSec = (now - lastNanos) / 1_000_000_000.0;
			if (elapsedSec <= 0) return;
			tokens = Math.min(capacity, tokens + elapsedSec * refillPerSec);
			lastNanos = now;
		}
	}
}
