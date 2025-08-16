package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;
import com.anonymouschat.anonymouschatserver.common.log.LogTag;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtTokenProvider;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtValidator;
import com.anonymouschat.anonymouschatserver.infra.token.TokenStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final TokenStorage tokenStorage;
	private final JwtTokenProvider jwtTokenProvider;
	private final JwtValidator jwtValidator;

	public String generateAccessToken(OAuthProvider provider, String providerId) {
		log.info("{}OAuth 로그인용 액세스 토큰 생성 요청 - provider={}, providerId={}", LogTag.AUTH, provider, providerId);
		return jwtTokenProvider.createAccessTokenForOAuthLogin(provider, providerId);
	}

	public String generateAccessToken(Long userId, String role) {
		log.info("{}액세스 토큰 생성 요청 - userId={}", LogTag.AUTH, userId);
		return jwtTokenProvider.createAccessToken(userId, role);
	}

	public String generateRefreshToken(Long userId, String role) {
		log.info("{}리프레시 토큰 생성 요청 - userId={}", LogTag.AUTH, userId);
		return jwtTokenProvider.createRefreshToken(userId, role);
	}

	public void saveRefreshToken(String userId, String refreshToken) {
		long remainingMillis = jwtTokenProvider.getExpirationMillis(refreshToken);
		tokenStorage.save(userId, refreshToken, remainingMillis);
		log.info("{}리프레시 토큰 저장 완료 - userId={}, 만료까지 {}ms", LogTag.AUTH, userId, remainingMillis);
	}

	public String findRefreshTokenOrThrow(String userId) {
		return tokenStorage.find(userId)
				       .orElseThrow(() -> {
					       log.warn("{}리프레시 토큰 조회 실패 - userId={}", LogTag.AUTH, userId);
					       return new InvalidTokenException(ErrorCode.INVALID_TOKEN);
				       });
	}

	public void revokeRefreshToken(String userId) {
		tokenStorage.delete(userId);
		log.info("{}리프레시 토큰 폐기 완료 - userId={}", LogTag.AUTH, userId);
	}

	public void validateToken(String token) {
		jwtValidator.validate(token);
	}

	public Long getUserIdFromToken(String token) {
		return jwtTokenProvider.getPrincipalFromToken(token).userId();
	}
}
