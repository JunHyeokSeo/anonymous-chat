package com.anonymouschat.anonymouschatserver.application.event;

import lombok.Builder;

import java.time.Instant;

/**
 * 푸시 알림이 필요한 상황에서 발행되는 이벤트
 * 온라인 수신자가 없어서 푸시 알림을 보내야 하는 경우에 사용됩니다.
 */
@Builder
public record PushNotificationRequired(
		Long roomId,
		Long senderId,
		Long messageId,
		String content,
		Instant occurredAt
) {
	public static PushNotificationRequired of(Long roomId, Long senderId, Long messageId, String content) {
		return PushNotificationRequired.builder()
				       .roomId(roomId)
				       .senderId(senderId)
				       .messageId(messageId)
				       .content(content)
				       .occurredAt(Instant.now())
				       .build();
	}
}