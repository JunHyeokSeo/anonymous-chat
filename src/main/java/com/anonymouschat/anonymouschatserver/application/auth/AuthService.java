package com.anonymouschat.anonymouschatserver.application.auth;

import com.anonymouschat.anonymouschatserver.config.jwt.JwtTokenProvider;
import com.anonymouschat.anonymouschatserver.domain.user.User;
import com.anonymouschat.anonymouschatserver.domain.user.UserRepository;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

	// refreshToken 저장소는 메모리 기반으로 우선 단순 처리
	private final Map<Long, String> refreshTokenStore = new ConcurrentHashMap<>();

	public LoginResponse login(String nickname) {
		User user = userRepository.findByNickname(nickname)
				            .orElseGet(() -> userRepository.save(new User(nickname)));

		String accessToken = jwtTokenProvider.createAccessToken(user.getId());
		String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

		refreshTokenStore.put(user.getId(), refreshToken);

		return new LoginResponse(accessToken, refreshToken);
	}

	public boolean validateRefreshToken(Long userId, String refreshToken) {
		return refreshToken.equals(refreshTokenStore.get(userId));
	}

	public void logout(Long userId) {
		refreshTokenStore.remove(userId);
	}
}
