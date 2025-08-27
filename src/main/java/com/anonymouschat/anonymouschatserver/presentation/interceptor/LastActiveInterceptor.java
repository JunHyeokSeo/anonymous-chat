package com.anonymouschat.anonymouschatserver.presentation.interceptor;

import com.anonymouschat.anonymouschatserver.domain.repository.UserRepository;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class LastActiveInterceptor implements HandlerInterceptor {

	private final UserRepository userRepository;
	private final Clock clock;

	// 최소 업데이트 간격 (분 단위)
	private static final long MIN_UPDATE_INTERVAL_MINUTES = 10;

	@Override
	@Transactional
	public boolean preHandle(@NonNull HttpServletRequest request,
	                         @NonNull HttpServletResponse response,
	                         @NonNull Object handler) {


		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return true;
		}

		log.debug("[INTERCEPTOR] LastActiveInterceptor 진입");
		Object principal = authentication.getPrincipal();
		if (principal instanceof CustomPrincipal customPrincipal) {
			Long userId = customPrincipal.userId();

			log.debug("[INTERCEPTOR] userId 추출 - userId={}", userId);
			if (userId != null) {
				userRepository.findById(userId).ifPresent(user -> {
					LocalDateTime now = LocalDateTime.now(clock);

					// N분 이상 차이날 때만 update
					if (user.getLastActiveAt() == null || user.getLastActiveAt().plusMinutes(MIN_UPDATE_INTERVAL_MINUTES).isBefore(now)) {
						user.updateLastActiveAt(clock);

						log.debug("[INTERCEPTOR] lastActiveAt 업데이트 완료 - userId={}, user={}", userId, user.getClass());
					}
				});
			}
		}

		return true;
	}
}
