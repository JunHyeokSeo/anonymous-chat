package com.anonymouschat.anonymouschatserver.application.listener;

import com.anonymouschat.anonymouschatserver.application.event.PushNotificationRequired;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

@ExtendWith(MockitoExtension.class)
@DisplayName("PushNotificationListener 테스트")
class PushNotificationListenerTest {

	@InjectMocks
	private PushNotificationListener pushNotificationListener;

	// TODO: FCM 서비스가 구현되면 Mock 추가
	// @Mock
	// private FcmService fcmService;

	@Test
	@DisplayName("푸시 알림 요청 이벤트를 정상적으로 처리한다")
	void handlePushNotificationRequired_Success() {
		// given
		PushNotificationRequired event = PushNotificationRequired.builder()
				                                 .roomId(1L)
				                                 .senderId(100L)
				                                 .messageId(1000L)
				                                 .content("안녕하세요!")
				                                 .occurredAt(Instant.now())
				                                 .build();

		// when & then - 현재는 예외가 발생하지 않는지만 확인
		// TODO: FCM 서비스 구현 후 실제 호출 검증 추가
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
			pushNotificationListener.handlePushNotificationRequired(event);
		});
	}

	@Test
	@DisplayName("빌더 패턴으로 생성된 이벤트를 정상적으로 처리한다")
	void handlePushNotificationRequired_WithBuilderEvent() {
		// given
		PushNotificationRequired event = PushNotificationRequired.of(
				2L, 200L, 2000L, "테스트 메시지"
		);

		// when & then
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
			pushNotificationListener.handlePushNotificationRequired(event);
		});
	}

	@Test
	@DisplayName("null 값이 포함된 이벤트도 안전하게 처리한다")
	void handlePushNotificationRequired_WithNullValues() {
		// given
		PushNotificationRequired event = PushNotificationRequired.builder()
				                                 .roomId(3L)
				                                 .senderId(300L)
				                                 .messageId(3000L)
				                                 .content(null) // null content
				                                 .occurredAt(Instant.now())
				                                 .build();

		// when & then
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
			pushNotificationListener.handlePushNotificationRequired(event);
		});
	}

	// TODO: FCM 서비스 구현 후 추가할 테스트들
    /*
    @Test
    @DisplayName("FCM 전송에 성공하면 로그가 남는다")
    void handlePushNotificationRequired_FcmSuccess() {
        // given
        PushNotificationRequired event = createTestEvent();

        // when
        pushNotificationListener.handlePushNotificationRequired(event);

        // then
        verify(fcmService).sendChatNotification(event);
        // 로그 검증 로직 추가
    }

    @Test
    @DisplayName("FCM 전송에 실패해도 예외가 전파되지 않는다")
    void handlePushNotificationRequired_FcmFailure() {
        // given
        PushNotificationRequired event = createTestEvent();
        doThrow(new RuntimeException("FCM 서버 오류"))
                .when(fcmService).sendChatNotification(event);

        // when & then
        assertDoesNotThrow(() -> {
            pushNotificationListener.handlePushNotificationRequired(event);
        });

        // 에러 로그 검증 로직 추가
    }
    */

	private PushNotificationRequired createTestEvent() {
		return PushNotificationRequired.builder()
				       .roomId(1L)
				       .senderId(100L)
				       .messageId(1000L)
				       .content("테스트 메시지")
				       .occurredAt(Instant.now())
				       .build();
	}
}