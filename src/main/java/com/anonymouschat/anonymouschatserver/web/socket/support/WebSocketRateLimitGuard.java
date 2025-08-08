package com.anonymouschat.anonymouschatserver.web.socket.support;

import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
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

	public boolean allow(long userId, MessageType type) {
		EnumMap<MessageType, TokenBucket> userBuckets =
				buckets.computeIfAbsent(userId, k -> new EnumMap<>(MessageType.class));

		TokenBucket bucket = userBuckets.computeIfAbsent(type, t -> switch (t) {
			case CHAT -> new TokenBucket(CHAT_CAPACITY, CHAT_REFILL_PER_SEC);
			case READ -> new TokenBucket(READ_CAPACITY, READ_REFILL_PER_SEC);
			case ENTER, LEAVE -> new TokenBucket(ENTER_LEAVE_CAPACITY, ENTER_LEAVE_REFILL_PER_SEC);
		});

		return bucket.tryConsume();
	}

	/** 연결 종료 시 메모리 정리를 위해 호출 (선택) */
	public void clear(long userId) {
		buckets.remove(userId);
	}

	/** 토큰 버킷 (thread-safe) */
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
