package com.anonymouschat.anonymouschatserver.application.event;

import lombok.Builder;

import java.time.Instant;

/**
 * 메시지 저장 실패 시 발행되는 이벤트
 * 재시도, 알림, 모니터링 용도로 사용됩니다.
 */
@Builder
public record MessageStoreFailure(
		Long roomId,
		Long senderId,
		String content,
		String errorMessage,
		Instant failedAt,
		int retryCount
) {
	/**
	 * 첫 번째 실패 시 사용하는 생성자
	 */
	public MessageStoreFailure(Long roomId, Long senderId, String content) {
		this(roomId, senderId, content, null, Instant.now(), 0);
	}

	/**
	 * 재시도를 위한 새로운 이벤트 생성
	 */
	public MessageStoreFailure withRetry(String errorMessage) {
		return MessageStoreFailure.builder()
				       .roomId(this.roomId)
				       .senderId(this.senderId)
				       .content(this.content)
				       .errorMessage(errorMessage)
				       .failedAt(Instant.now())
				       .retryCount(this.retryCount + 1)
				       .build();
	}
}