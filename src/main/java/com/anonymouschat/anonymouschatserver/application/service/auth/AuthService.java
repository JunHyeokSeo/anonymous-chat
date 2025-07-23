package com.anonymouschat.anonymouschatserver.application.service.auth;

import com.anonymouschat.anonymouschatserver.common.jwt.JwtTokenProvider;
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

	// userId -> refreshToken
	private final Map<Long, String> refreshTokenStore = new ConcurrentHashMap<>();

	/**
	 * refreshToken을 이용해 accessToken 재발급
	 */
//	public RefreshTokenResponse reissueAccessToken(String refreshToken) {
//		if (!jwtTokenProvider.validateToken(refreshToken)) {
//			throw new JwtException("유효하지 않은 Refresh Token 입니다.");
//		}
//
//		Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
//		if (!isValidRefreshToken(userId, refreshToken)) {
//			throw new IllegalStateException("저장된 토큰과 일치하지 않습니다.");
//		}
//
//		String newAccessToken = jwtTokenProvider.createUserToken(userId);
//		String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
//		refreshTokenStore.put(userId, newRefreshToken);
//
//		return new RefreshTokenResponse(newAccessToken, newRefreshToken);
//	}

	/**
	 * 로그아웃 시 RefreshToken 폐기
	 */
	public void logout(Long userId) {
		refreshTokenStore.remove(userId);
	}

	/**
	 * 회원가입 완료 시 최초 refreshToken 저장
	 */
	public void saveRefreshToken(Long userId, String refreshToken) {
		refreshTokenStore.put(userId, refreshToken);
	}

	private boolean isValidRefreshToken(Long userId, String refreshToken) {
		return refreshToken.equals(refreshTokenStore.get(userId));
	}
}
