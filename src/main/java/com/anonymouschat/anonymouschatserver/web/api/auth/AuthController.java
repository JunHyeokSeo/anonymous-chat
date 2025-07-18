package com.anonymouschat.anonymouschatserver.web.api.auth;

import com.anonymouschat.anonymouschatserver.application.auth.AuthService;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.LoginRequest;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.LoginResponse;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.RefreshTokenRequest;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.RefreshTokenResponse;
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
		return ResponseEntity.ok(authService.loginWithNickname(request.nickname()));
	}

	@PostMapping("/refresh")
	public ResponseEntity<RefreshTokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
		return ResponseEntity.ok(authService.reissueAccessToken(request.refreshToken()));
	}
}
