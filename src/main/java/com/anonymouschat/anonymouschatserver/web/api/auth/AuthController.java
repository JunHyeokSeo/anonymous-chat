package com.anonymouschat.anonymouschatserver.web.api.auth;

import com.anonymouschat.anonymouschatserver.application.auth.AuthService;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.LoginRequest;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.LoginResponse;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.RefreshTokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request.nickname()));
	}

	//todo: 이거 서비스랑 분리부터 시작
	@PostMapping("/refresh")
	public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
		String refreshToken = request.refreshToken();

		// 토큰 유효성 확인
		if (!jwtTokenProvider.validateToken(refreshToken)) {
			throw new JwtException("유효하지 않은 Refresh Token입니다.");
		}

		Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
		String newAccessToken = jwtTokenProvider.createAccessToken(userId);
		String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

		return ResponseEntity.ok(new TokenResponse(newAccessToken, newRefreshToken));
	}
}
