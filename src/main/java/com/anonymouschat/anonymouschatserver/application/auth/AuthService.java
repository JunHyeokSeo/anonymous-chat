package com.anonymouschat.anonymouschatserver.application.auth;

import com.anonymouschat.anonymouschatserver.config.jwt.JwtTokenProvider;
import com.anonymouschat.anonymouschatserver.domain.user.User;
import com.anonymouschat.anonymouschatserver.domain.user.UserRepository;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.LoginResponse;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.RefreshTokenResponse;
import com.nimbusds.oauth2.sdk.TokenResponse;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.management.JMException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

	// 간단한 메모리 기반 저장소 (실무에서는 Redis 추천)
	private final Map<Long, String> refreshTokenStore = new ConcurrentHashMap<>();

	public LoginResponse loginWithNickname(String nickname) {
		User user = userRepository.findByNickname(nickname)
				            .orElseGet(() -> userRepository.save(new User(nickname)));

		String accessToken = jwtTokenProvider.createAccessToken(user.getId());
		String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

		refreshTokenStore.put(user.getId(), refreshToken);

		return new LoginResponse(accessToken, refreshToken);
	}

	public RefreshTokenResponse reissueAccessToken(String refreshToken) {
		if (!jwtTokenProvider.validateToken(refreshToken)) {
			throw new JwtException("유효하지 않은 Refresh Token입니다.");
		}

		Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
		if (!isValidRefreshToken(userId, refreshToken)) {
			throw new IllegalStateException("저장된 토큰과 일치하지 않습니다.");
		}

		String newAccessToken = jwtTokenProvider.createAccessToken(userId);
		String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
		refreshTokenStore.put(userId, newRefreshToken);

		return new RefreshTokenResponse(newAccessToken, newRefreshToken);
	}

	private boolean isValidRefreshToken(Long userId, String refreshToken) {
		return refreshToken.equals(refreshTokenStore.get(userId));
	}

	public void logout(Long userId) {
		refreshTokenStore.remove(userId);
	}
}
