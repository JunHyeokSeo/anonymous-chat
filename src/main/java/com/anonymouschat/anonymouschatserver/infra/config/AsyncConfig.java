package com.anonymouschat.anonymouschatserver.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 처리를 위한 설정 클래스입니다.
 * 채팅 메시지 저장과 푸시 알림 등의 비동기 작업을 위한 스레드 풀을 구성합니다.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

	/**
	 * 채팅 메시지 처리를 위한 전용 스레드 풀을 생성합니다.
	 * 메시지 저장, 푸시 알림 등의 작업에 사용됩니다.
	 */
	@Bean(name = "chatMessageExecutor")
	public Executor chatMessageExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);           // 기본 스레드 수
		executor.setMaxPoolSize(10);           // 최대 스레드 수
		executor.setQueueCapacity(100);        // 큐 대기 용량
		executor.setThreadNamePrefix("Chat-"); // 스레드 이름 접두사
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(30);
		executor.initialize();
		return executor;
	}
}