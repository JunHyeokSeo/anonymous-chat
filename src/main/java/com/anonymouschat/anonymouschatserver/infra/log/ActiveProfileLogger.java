package com.anonymouschat.anonymouschatserver.infra.log;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActiveProfileLogger {

	private final Environment environment;

	@PostConstruct
	public void logActiveProfiles() {
		String[] activeProfiles = environment.getActiveProfiles();
		log.info("[SYSTEM] 현재 활성화된 프로파일: {}", String.join(", ", activeProfiles));
	}
}
