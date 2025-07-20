package com.anonymouschat.anonymouschatserver.application.service.auth;

import com.anonymouschat.anonymouschatserver.common.jwt.JwtTokenProvider;
import com.anonymouschat.anonymouschatserver.common.jwt.OAuthPrincipal;
import com.anonymouschat.anonymouschatserver.domain.user.OAuthProvider;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.RefreshTokenResponse;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final JwtTokenProvider jwtTokenProvider;

	private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();

	public RefreshTokenResponse reissueAccessToken(String refreshToken) {
		if (!jwtTokenProvider.validateToken(refreshToken)) {
			throw new JwtException("유효하지 않은 Refresh Token 입니다.");
		}

		OAuthPrincipal userInfo = jwtTokenProvider.getUserInfoFromToken(refreshToken);
		String key = buildKey(userInfo.provider(), userInfo.providerId());
		if (!isValidRefreshToken(key, refreshToken)) {
			throw new IllegalStateException("저장된 토큰과 일치하지 않습니다.");
		}

		String newAccessToken = jwtTokenProvider.createAccessToken(userInfo.provider(), userInfo.providerId());
		String newRefreshToken = jwtTokenProvider.createRefreshToken(userInfo.provider(), userInfo.providerId());
		refreshTokenStore.put(buildKey(userInfo.provider(), userInfo.providerId()), newRefreshToken);

		return new RefreshTokenResponse(newAccessToken, newRefreshToken);
	}

	private boolean isValidRefreshToken(String key, String refreshToken) {
		return refreshToken.equals(refreshTokenStore.get(key));
	}

	public void logout(OAuthProvider provider, String providerId) {
		refreshTokenStore.remove(buildKey(provider, providerId));
	}

	private String buildKey(OAuthProvider provider, String providerId) {
		return provider.name() + ":" + providerId;
	}
}
