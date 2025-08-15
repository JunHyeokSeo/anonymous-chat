package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.AuthTokens;
import com.anonymouschat.anonymouschatserver.application.service.AuthService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthUseCase {

    private final AuthService authService;
    private final UserService userService;

    @Transactional
    public AuthTokens login(OAuthProvider provider, String providerId) {
        User user = userService.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> userService.createGuestUser(provider, providerId));

        String accessToken = authService.generateAccessToken(provider, providerId);
        String refreshToken = null;

        if (user.getRole() != UserRole.ROLE_GUEST) {
            refreshToken = authService.generateRefreshToken(user.getId(), user.getRole().getAuthority());
            authService.saveRefreshToken(user.getId().toString(), refreshToken);
        }

        return new AuthTokens(accessToken, refreshToken);
    }

	@Transactional
	public AuthTokens refresh(String oldRefreshToken) {
		authService.validateToken(oldRefreshToken);

		Long userId = authService.getUserIdFromToken(oldRefreshToken);
		String storedRefreshToken = authService.findRefreshTokenOrThrow(userId.toString());

		if (!storedRefreshToken.equals(oldRefreshToken)) {
			log.warn("RefreshToken 탈취 가능성 감지. userId = {}, token = {}", userId, oldRefreshToken);
			authService.revokeRefreshToken(userId.toString());
			throw new InvalidTokenException(ErrorCode.TOKEN_THEFT_DETECTED);
		}

		authService.revokeRefreshToken(userId.toString());

		User user = userService.findUser(userId);
		String newAccessToken = authService.generateAccessToken(user.getId(), user.getRole().getAuthority());
		String newRefreshToken = authService.generateRefreshToken(user.getId(), user.getRole().getAuthority());
		authService.saveRefreshToken(user.getId().toString(), newRefreshToken);

		return new AuthTokens(newAccessToken, newRefreshToken);
	}


	@Transactional
    public void logout(String userId) {
        authService.revokeRefreshToken(userId);
    }
}
