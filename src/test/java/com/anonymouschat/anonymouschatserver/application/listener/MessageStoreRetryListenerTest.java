package com.anonymouschat.anonymouschatserver.application.listener;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.event.MessageStoreFailure;
import com.anonymouschat.anonymouschatserver.application.usecase.MessageUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageStoreRetryListener 테스트")
class MessageStoreRetryListenerTest {

	@Mock
	private MessageUseCase messageUseCase;

	@Mock
	private ApplicationEventPublisher publisher;

	@InjectMocks
	private MessageStoreRetryListener retryListener;

	@Test
	@DisplayName("첫 번째 재시도에서 성공하면 메시지가 저장된다")
	void handleMessageStoreFailure_FirstRetrySuccess() throws InterruptedException {
		// given
		MessageStoreFailure event = new MessageStoreFailure(1L, 100L, "테스트 메시지");
		Long expectedMessageId = 1000L;

		when(messageUseCase.sendMessage(any(MessageUseCaseDto.SendMessageRequest.class)))
				.thenReturn(expectedMessageId);

		// when
		retryListener.handleMessageStoreFailure(event);

		// then
		verify(messageUseCase).sendMessage(argThat(request ->
				                                           request.roomId().equals(1L) &&
						                                           request.senderId().equals(100L) &&
						                                           request.content().equals("테스트 메시지")
		));

		// 성공했으므로 재시도 이벤트가 발행되지 않아야 함
		verify(publisher, never()).publishEvent(any(MessageStoreFailure.class));
	}

	@Test
	@DisplayName("재시도에서 실패하면 새로운 실패 이벤트를 발행한다")
	void handleMessageStoreFailure_RetryFailed() throws InterruptedException {
		// given
		MessageStoreFailure event = MessageStoreFailure.builder()
				                            .roomId(1L)
				                            .senderId(100L)
				                            .content("테스트 메시지")
				                            .retryCount(1)
				                            .failedAt(Instant.now())
				                            .build();

		String errorMessage = "DB 연결 실패";
		when(messageUseCase.sendMessage(any(MessageUseCaseDto.SendMessageRequest.class)))
				.thenThrow(new RuntimeException(errorMessage));

		// when
		retryListener.handleMessageStoreFailure(event);

		// then
		ArgumentCaptor<MessageStoreFailure> eventCaptor = ArgumentCaptor.forClass(MessageStoreFailure.class);
		verify(publisher).publishEvent(eventCaptor.capture());

		MessageStoreFailure publishedEvent = eventCaptor.getValue();
		assertThat(publishedEvent.roomId()).isEqualTo(1L);
		assertThat(publishedEvent.senderId()).isEqualTo(100L);
		assertThat(publishedEvent.content()).isEqualTo("테스트 메시지");
		assertThat(publishedEvent.retryCount()).isEqualTo(2); // 재시도 횟수 증가
		assertThat(publishedEvent.errorMessage()).isEqualTo(errorMessage);
	}

	@Test
	@DisplayName("최대 재시도 횟수에 도달하면 영구 실패로 처리한다")
	void handleMessageStoreFailure_MaxRetryReached() {
		// given
		MessageStoreFailure event = MessageStoreFailure.builder()
				                            .roomId(1L)
				                            .senderId(100L)
				                            .content("테스트 메시지")
				                            .retryCount(3) // MAX_RETRY_COUNT와 같음
				                            .failedAt(Instant.now())
				                            .build();

		// when
		retryListener.handleMessageStoreFailure(event);

		// then
		verify(messageUseCase, never()).sendMessage(any());
		verify(publisher, never()).publishEvent(any());

		// TODO: 모니터링 서비스 Mock 추가 후 알림 전송 검증
	}

	@Test
	@DisplayName("재시도 횟수가 최대값을 초과하면 영구 실패로 처리한다")
	void handleMessageStoreFailure_ExceedsMaxRetry() {
		// given
		MessageStoreFailure event = MessageStoreFailure.builder()
				                            .roomId(1L)
				                            .senderId(100L)
				                            .content("테스트 메시지")
				                            .retryCount(5) // MAX_RETRY_COUNT 초과
				                            .failedAt(Instant.now())
				                            .build();

		// when
		retryListener.handleMessageStoreFailure(event);

		// then
		verify(messageUseCase, never()).sendMessage(any());
		verify(publisher, never()).publishEvent(any());
	}

	@Test
	@DisplayName("재시도 지연시간이 올바르게 계산된다")
	void calculateBackoffDelay_CorrectCalculation() {
		// given & when
		long delay0 = (Long) ReflectionTestUtils.invokeMethod(retryListener, "calculateBackoffDelay", 0);
		long delay1 = (Long) ReflectionTestUtils.invokeMethod(retryListener, "calculateBackoffDelay", 1);
		long delay2 = (Long) ReflectionTestUtils.invokeMethod(retryListener, "calculateBackoffDelay", 2);
		long delay3 = (Long) ReflectionTestUtils.invokeMethod(retryListener, "calculateBackoffDelay", 3);

		// then
		assertThat(delay0).isEqualTo(1000L);  // 1초
		assertThat(delay1).isEqualTo(2000L);  // 2초
		assertThat(delay2).isEqualTo(4000L);  // 4초
		assertThat(delay3).isEqualTo(8000L);  // 8초
	}

	@Test
	@DisplayName("withRetry 메서드가 새로운 이벤트를 올바르게 생성한다")
	void messageStoreFailure_WithRetry() {
		// given
		MessageStoreFailure originalEvent = MessageStoreFailure.builder()
				                                    .roomId(1L)
				                                    .senderId(100L)
				                                    .content("테스트 메시지")
				                                    .retryCount(1)
				                                    .failedAt(Instant.now())
				                                    .build();

		String newErrorMessage = "새로운 오류";

		// when
		MessageStoreFailure retryEvent = originalEvent.withRetry(newErrorMessage);

		// then
		assertThat(retryEvent.roomId()).isEqualTo(originalEvent.roomId());
		assertThat(retryEvent.senderId()).isEqualTo(originalEvent.senderId());
		assertThat(retryEvent.content()).isEqualTo(originalEvent.content());
		assertThat(retryEvent.retryCount()).isEqualTo(2); // 증가
		assertThat(retryEvent.errorMessage()).isEqualTo(newErrorMessage);
		assertThat(retryEvent.failedAt()).isAfter(originalEvent.failedAt());
	}

	@Test
	@DisplayName("첫 번째 실패 생성자가 올바르게 동작한다")
	void messageStoreFailure_FirstFailureConstructor() {
		// given & when
		MessageStoreFailure event = new MessageStoreFailure(1L, 100L, "테스트 메시지");

		// then
		assertThat(event.roomId()).isEqualTo(1L);
		assertThat(event.senderId()).isEqualTo(100L);
		assertThat(event.content()).isEqualTo("테스트 메시지");
		assertThat(event.retryCount()).isZero();
		assertThat(event.errorMessage()).isNull();
		assertThat(event.failedAt()).isNotNull();
	}

	@Test
	@DisplayName("Thread.sleep 중 InterruptedException이 발생해도 안전하게 처리된다")
	void handleMessageStoreFailure_InterruptedException() {
		// given
		MessageStoreFailure event = new MessageStoreFailure(1L, 100L, "테스트 메시지");

		// 현재 스레드를 중단 상태로 만듦
		Thread.currentThread().interrupt();

		// when & then - 예외가 전파되지 않아야 함
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
			retryListener.handleMessageStoreFailure(event);
		});

		// 재시도 이벤트가 발행되었는지 확인
		verify(publisher).publishEvent(any(MessageStoreFailure.class));

		// 인터럽트 플래그가 처리되었는지 확인 (이미 consumed 되었을 수 있음)
		// InterruptedException 발생 시 Thread.sleep()에서 인터럽트 플래그가 자동으로 클리어됨
	}

	@Test
	@DisplayName("인터럽트 상태에서 시작하면 InterruptedException이 발생하고 올바르게 처리된다")
	void handleMessageStoreFailure_InterruptedStateAtStart() {
		// given
		MessageStoreFailure event = new MessageStoreFailure(1L, 100L, "테스트 메시지");

		// 현재 스레드를 중단 상태로 만듦
		Thread.currentThread().interrupt();

		// 초기 인터럽트 상태 확인
		assertThat(Thread.currentThread().isInterrupted()).isTrue();

		// when
		retryListener.handleMessageStoreFailure(event);

		// then
		// InterruptedException으로 인한 재시도 이벤트 발행 확인
		ArgumentCaptor<MessageStoreFailure> eventCaptor = ArgumentCaptor.forClass(MessageStoreFailure.class);
		verify(publisher).publishEvent(eventCaptor.capture());

		MessageStoreFailure publishedEvent = eventCaptor.getValue();
		assertThat(publishedEvent.errorMessage()).contains("interrupted");

		// 인터럽트 상태가 복원되었는지 확인
		assertThat(Thread.currentThread().isInterrupted()).isTrue();

		// 테스트 정리를 위해 인터럽트 상태 클리어
		Thread.interrupted();
	}
}