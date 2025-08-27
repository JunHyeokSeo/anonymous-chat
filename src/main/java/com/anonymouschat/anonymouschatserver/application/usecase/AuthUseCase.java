package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.AuthUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.service.AuthService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
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
	public AuthUseCaseDto.AuthData login(OAuthProvider provider, String providerId) {
		User user = userService.findByProviderAndProviderId(provider, providerId)
				            .orElseGet(() -> userService.createGuestUser(provider, providerId));

		return authService.issueTokensForUser(user, null, null);
	}

	@Transactional
	public AuthUseCaseDto.AuthTokens refreshByUserId(Long userId, String userAgent, String ipAddress) {
		var storedTokenInfo = authService.getRefreshTokenInfo(userId);

		authService.validateToken(storedTokenInfo.token());

		authService.invalidateRefreshToken(userId);

		User user = userService.findUser(userId);
		String newAccessToken = authService.createAccessToken(user.getId(), user.getRole());
		String newRefreshToken = authService.createRefreshToken(user.getId(), user.getRole());

		authService.saveRefreshToken(userId, newRefreshToken, userAgent, ipAddress);

		// 클라이언트에는 AccessToken만 반환
		return AuthUseCaseDto.AuthTokens.builder()
				       .accessToken(newAccessToken)
				       .build();
	}

	@Transactional
	public void logout(Long userId, String accessToken) {
		authService.invalidateRefreshToken(userId);
		authService.blacklistAccessToken(accessToken);
	}

	@Transactional
	public AuthUseCaseDto.AuthData handleOAuthCallback(String code, String userAgent, String ipAddress) {
		AuthUseCaseDto.AuthTempData tempData = authService.consumeOAuthTempData(code);

		User user = userService.findUser(tempData.userId());

		return authService.issueTokensForUser(user, userAgent, ipAddress);
	}

	public String storeOAuthTempData(AuthUseCaseDto.AuthData authResult) {
		AuthUseCaseDto.AuthTempData tempData = AuthUseCaseDto.AuthTempData.builder()
				                                        .accessToken(authResult.accessToken())
				                                        .refreshToken(authResult.refreshToken())
				                                        .isGuestUser(authResult.isGuestUser())
				                                        .userId(authResult.userId())
				                                        .userNickname(authResult.userNickname())
				                                        .build();

		return authService.storeOAuthTempData(tempData);
	}

	public AuthUseCaseDto.AuthTempData consumeOAuthTempData(String code) {
		return authService.consumeOAuthTempData(code);
	}

	public void saveRefreshToken(Long userId, String refreshToken, String userAgent, String ipAddress) {
		authService.saveRefreshToken(userId, refreshToken, userAgent, ipAddress);
	}

	public Long getUserIdFromToken(String token) {
		return authService.extractUserId(token);
	}
}