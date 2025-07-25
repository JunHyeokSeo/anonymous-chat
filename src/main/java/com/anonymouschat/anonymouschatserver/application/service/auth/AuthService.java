package com.anonymouschat.anonymouschatserver.application.service.auth;

import com.anonymouschat.anonymouschatserver.common.jwt.JwtTokenProvider;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.RefreshTokenResponse;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenStore refreshTokenStore;

	public RefreshTokenResponse reissueAccessToken(String refreshToken) {
		if (!jwtTokenProvider.validateToken(refreshToken)) {
			throw new JwtException("유효하지 않은 Refresh Token 입니다.");
		}

		Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
		if (!refreshTokenStore.matches(userId, refreshToken)) {
			throw new IllegalStateException("저장된 토큰과 일치하지 않습니다.");
		}

		String newAccessToken = jwtTokenProvider.createAccessToken(userId);
		String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
		refreshTokenStore.save(userId, newRefreshToken);

		return new RefreshTokenResponse(newAccessToken, newRefreshToken);
	}

	public void logout(Long userId) {
		refreshTokenStore.remove(userId);
	}

	public void saveRefreshToken(Long userId, String refreshToken) {
		refreshTokenStore.save(userId, refreshToken);
	}
}
