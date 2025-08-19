package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.AuthUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.service.AuthService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;
import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthUseCase {

    private final AuthService authService;
    private final UserService userService;

    @Transactional
    public AuthUseCaseDto.AuthResult login(OAuthProvider provider, String providerId) {
	    AtomicBoolean isGuestUser = new AtomicBoolean(false);

	    User user = userService.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> userService.createGuestUser(provider, providerId));

        String accessToken = authService.generateAccessToken(provider, providerId);
        String refreshToken = null;

	    if (user.getRole() != Role.GUEST) {
		    refreshToken = authService.generateRefreshToken(user.getId(), user.getRole());
		    authService.saveRefreshToken(user.getId(), refreshToken);
	    } else {
			isGuestUser.set(true);
	    }

	    return AuthUseCaseDto.AuthResult.builder()
			           .accessToken(accessToken)
			           .refreshToken(refreshToken)
			           .isGuestUser(isGuestUser.get())
			           .build();
    }

	@Transactional
	public AuthUseCaseDto.AuthTokens refresh(String oldRefreshToken) {
		authService.validateToken(oldRefreshToken);

		Long userId = authService.getUserIdFromToken(oldRefreshToken);
		String storedRefreshToken = authService.findRefreshTokenOrThrow(userId);

		if (!storedRefreshToken.equals(oldRefreshToken)) {
			log.warn("{}다른 기기에서의 RefreshToken 사용 감지 - userId = {}, token = {}", LogTag.SECURITY, userId, oldRefreshToken);
			authService.revokeRefreshToken(userId);
			throw new InvalidTokenException(ErrorCode.TOKEN_THEFT_DETECTED);
		}

		authService.revokeRefreshToken(userId);

		User user = userService.findUser(userId);
		String newAccessToken = authService.generateAccessToken(user.getId(), user.getRole());
		String newRefreshToken = authService.generateRefreshToken(user.getId(), user.getRole());
		authService.saveRefreshToken(user.getId(), newRefreshToken);

		return AuthUseCaseDto.AuthTokens.builder()
				       .accessToken(newAccessToken)
				       .refreshToken(newRefreshToken)
				       .build();
	}


	@Transactional
    public void logout(Long userId) {
        log.info("{}로그아웃 요청 - userId={}", LogTag.AUTH, userId);
        authService.revokeRefreshToken(userId);
    }
}
