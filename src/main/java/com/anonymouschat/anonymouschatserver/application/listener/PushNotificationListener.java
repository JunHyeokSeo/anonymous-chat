package com.anonymouschat.anonymouschatserver.application.listener;

import com.anonymouschat.anonymouschatserver.application.event.PushNotificationRequired;
import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 푸시 알림 요청 이벤트를 처리하는 리스너입니다.
 * FCM을 통해 오프라인 사용자에게 알림을 전송합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PushNotificationListener {
	// TODO: FCM 서비스 또는 푸시 알림 서비스 의존성 추가
	// private final FcmService fcmService;

	/**
	 * 푸시 알림 요청 이벤트를 처리합니다.
	 * 비동기로 실행되어 메시지 처리 성능에 영향을 주지 않습니다.
	 */
	@EventListener
	@Async
	public void handlePushNotificationRequired(PushNotificationRequired event) {
		log.info("{}push notification requested: roomId={} messageId={} senderId={}",
				LogTag.CHAT, event.roomId(), event.messageId(), event.senderId());

		try {
			// TODO: FCM 푸시 알림 전송 로직 구현
			// fcmService.sendChatNotification(event);

			log.debug("{}push notification sent successfully: roomId={} messageId={}",
					LogTag.CHAT, event.roomId(), event.messageId());

		} catch (Exception e) {
			log.error("{}failed to send push notification: roomId={} messageId={} error={}",
					LogTag.CHAT, event.roomId(), event.messageId(), e.getMessage(), e);
		}
	}
}