package com.anonymouschat.anonymouschatserver.application.listener;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.event.MessageStoreFailure;
import com.anonymouschat.anonymouschatserver.application.usecase.MessageUseCase;
import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 메시지 저장 실패 시 재시도를 처리하는 리스너입니다.
 * 최대 재시도 횟수까지 메시지 저장을 시도합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageStoreRetryListener {
	private final MessageUseCase messageUseCase;
	private final ApplicationEventPublisher publisher;

	private static final int MAX_RETRY_COUNT = 3;
	private static final long BASE_DELAY_MS = 1000L; // 1초

	/**
	 * 메시지 저장 실패 이벤트를 처리합니다.
	 * 최대 재시도 횟수에 도달하지 않았다면 재시도를 수행합니다.
	 */
	@EventListener
	@Async
	public void handleMessageStoreFailure(MessageStoreFailure event) {
		if (event.retryCount() >= MAX_RETRY_COUNT) {
			log.error("{}message save permanently failed after {} retries: roomId={} senderId={}",
					LogTag.CHAT, event.retryCount(), event.roomId(), event.senderId());

			// TODO: 모니터링 시스템에 알림 전송 (예: Slack, 이메일 등)
			// monitoringService.notifyPermanentFailure(event);
			return;
		}

		long delay = calculateBackoffDelay(event.retryCount());
		log.info("{}retrying message save after {}ms: roomId={} senderId={} attempt={}",
				LogTag.CHAT, delay, event.roomId(), event.senderId(), event.retryCount() + 1);

		try {
			Thread.sleep(delay);

			Long messageId = messageUseCase.sendMessage(MessageUseCaseDto.SendMessageRequest.builder()
					                                            .roomId(event.roomId())
					                                            .senderId(event.senderId())
					                                            .content(event.content())
					                                            .build());

			log.info("{}message save retry successful: roomId={} senderId={} messageId={}",
					LogTag.CHAT, event.roomId(), event.senderId(), messageId);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // 인터럽트 상태 복원
			log.warn("{}message save retry interrupted: roomId={} senderId={} attempt={}",
					LogTag.CHAT, event.roomId(), event.senderId(), event.retryCount() + 1);

			// 인터럽트된 경우에도 재시도 이벤트 발행
			publisher.publishEvent(event.withRetry("Thread interrupted"));

		} catch (Exception e) {
			log.warn("{}message save retry failed: roomId={} senderId={} attempt={} error={}",
					LogTag.CHAT, event.roomId(), event.senderId(), event.retryCount() + 1, e.getMessage());

			// 재시도 이벤트 발행
			publisher.publishEvent(event.withRetry(e.getMessage()));
		}
	}

	/**
	 * 지수 백오프 지연시간을 계산합니다.
	 */
	private long calculateBackoffDelay(int retryCount) {
		return BASE_DELAY_MS * (long) Math.pow(2, retryCount);
	}
}