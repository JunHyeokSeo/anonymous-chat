package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.application.dto.AuthServiceDto;
import com.anonymouschat.anonymouschatserver.application.dto.AuthUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.port.TokenStoragePort;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Role;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtTokenProvider;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final JwtTokenProvider jwtProvider;
	private final JwtValidator jwtValidator;
	private final TokenStoragePort tokenStorage;

	public String createAccessToken(OAuthProvider provider, String providerId) {
		return jwtProvider.createAccessTokenForOAuthLogin(provider, providerId);
	}

	public String createAccessToken(Long userId, Role role) {
		return jwtProvider.createAccessToken(userId, role);
	}

	public String createRefreshToken(Long userId, Role role) {
		return jwtProvider.createRefreshToken(userId, role);
	}

	public void validateToken(String token) {
		jwtValidator.validate(token);

		String tokenHash = hashToken(token);
		if (tokenStorage.isTokenBlacklisted(tokenHash)) {
			throw new InvalidTokenException(ErrorCode.INVALID_TOKEN);
		}
	}

	public Long extractUserId(String token) {
		return jwtProvider.getPrincipalFromToken(token).userId();
	}

	public void saveRefreshToken(Long userId, String refreshToken, String userAgent, String ipAddress) {
		AuthServiceDto.RefreshTokenInfo info = AuthServiceDto.RefreshTokenInfo
				                                       .create(userId, refreshToken, userAgent, ipAddress);
		tokenStorage.storeRefreshToken(userId, info);
	}

	public AuthServiceDto.RefreshTokenInfo getRefreshTokenInfo(Long userId) {
		return tokenStorage.getRefreshToken(userId)
				       .orElseThrow(() -> new InvalidTokenException(ErrorCode.INVALID_TOKEN));
	}

	public void invalidateRefreshToken(Long userId) {
		tokenStorage.deleteRefreshToken(userId);
	}

	public void blacklistAccessToken(String accessToken) {
		if (isTokenExpired(accessToken)) return;

		String tokenHash = hashToken(accessToken);
		int ttl = (int) (jwtProvider.getExpirationMillis(accessToken) / 1000);
		tokenStorage.blacklistToken(tokenHash, ttl);
	}

	public void revokeAllUserTokens(Long userId) {
		tokenStorage.deleteAllUserTokens(userId);
	}

	public String storeOAuthTempData(AuthUseCaseDto.AuthTempData data) {
		AuthServiceDto.OAuthTempInfo info = AuthServiceDto.OAuthTempInfo.from(data);
		return tokenStorage.storeOAuthTempData(info);
	}

	public AuthUseCaseDto.AuthTempData consumeOAuthTempData(String code) {
		return tokenStorage.consumeOAuthTempData(code)
				       .map(AuthServiceDto.OAuthTempInfo::toUseCaseDto)
				       .orElseThrow(() -> new InvalidTokenException(ErrorCode.INVALID_TOKEN));
	}

	public AuthUseCaseDto.AuthData issueTokensForUser(User user, String userAgent, String ipAddress) {
		String accessToken = createAccessToken(user.getId(), user.getRole());
		String refreshToken = null;
		boolean isGuestUser = user.getRole() == Role.GUEST;

		if (!isGuestUser) {
			refreshToken = createRefreshToken(user.getId(), user.getRole());
			saveRefreshToken(user.getId(), refreshToken, userAgent, ipAddress);
		}

		return AuthUseCaseDto.AuthData.builder()
				       .accessToken(accessToken)
				       .refreshToken(refreshToken)
				       .isGuestUser(isGuestUser)
				       .userId(user.getId())
				       .userNickname(user.getNickname())
				       .build();
	}

	private String hashToken(String token) {
		return String.valueOf(token.hashCode());
	}

	private boolean isTokenExpired(String token) {
		return jwtProvider.getExpirationMillis(token) <= 0;
	}
}