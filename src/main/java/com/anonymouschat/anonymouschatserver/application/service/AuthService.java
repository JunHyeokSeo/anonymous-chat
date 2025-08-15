package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtTokenProvider;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtValidator;
import com.anonymouschat.anonymouschatserver.infra.token.TokenStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenStorage tokenStorage;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtValidator jwtValidator;

    public String generateAccessToken(OAuthProvider provider, String providerId) {
        return jwtTokenProvider.createAccessTokenForOAuthLogin(provider, providerId);
    }

    public String generateAccessToken(Long userId, String role) {
        return jwtTokenProvider.createAccessToken(userId, role);
    }

    public String generateRefreshToken(Long userId, String role) {
        return jwtTokenProvider.createRefreshToken(userId, role);
    }

	public void saveRefreshToken(String userId, String refreshToken) {
		long remainingMillis = jwtTokenProvider.getExpirationMillis(refreshToken);
		tokenStorage.save(userId, refreshToken, remainingMillis);
	}

	public String findRefreshTokenOrThrow(String userId) {
		return tokenStorage.find(userId)
				       .orElseThrow(() -> new InvalidTokenException(ErrorCode.INVALID_TOKEN));
	}


	public void revokeRefreshToken(String userId) {
        tokenStorage.delete(userId);
    }

    public void validateToken(String token) {
        jwtValidator.validate(token);
    }

    public Long getUserIdFromToken(String token) {
        return jwtTokenProvider.getPrincipalFromToken(token).userId();
    }
}
