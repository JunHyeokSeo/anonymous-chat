package com.anonymouschat.anonymouschatserver.web.api.auth;

import com.anonymouschat.anonymouschatserver.application.service.auth.AuthService;
import com.anonymouschat.anonymouschatserver.common.code.SuccessCode;
import com.anonymouschat.anonymouschatserver.common.response.ApiResponse;
import com.anonymouschat.anonymouschatserver.domain.user.User;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.LoginRequest;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.LoginResponse;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.RefreshTokenRequest;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.RefreshTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(@RequestBody RefreshTokenRequest request) {
		return ResponseEntity.ok(ApiResponse.success(authService.reissueAccessToken(request.refreshToken())));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal User user) {
		authService.logout(user.getProvider(), user.getProviderId());
		return ResponseEntity.ok(ApiResponse.success(SuccessCode.USER_LOGGED_OUT));
	}

}
