package com.anonymouschat.anonymouschatserver.web.api.controller;

import com.anonymouschat.anonymouschatserver.application.usecase.AuthUseCase;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.web.ApiResponse;
import com.anonymouschat.anonymouschatserver.web.api.dto.AuthDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthUseCase authUseCase;

	/**
	 * 토큰 재발급 (ROLE_USER, ROLE_ADMIN 허용)
	 */
	@PostMapping("/refresh")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public ResponseEntity<ApiResponse<AuthDto.AuthTokensResponse>> refresh(
			@Valid @RequestBody AuthDto.RefreshRequest request
	) {
		var tokens = authUseCase.refresh(request.refreshToken());
		return ResponseEntity
				       .status(HttpStatus.OK)
				       .body(ApiResponse.success(AuthDto.AuthTokensResponse.from(tokens)));
	}

	/**
	 * 로그아웃 (ROLE_USER, ROLE_ADMIN 허용)
	 */
	@PostMapping("/logout")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public ResponseEntity<ApiResponse<Void>> logout(
			@AuthenticationPrincipal CustomPrincipal principal
	) {
		authUseCase.logout(principal.userId());
		return ResponseEntity
				       .status(HttpStatus.OK)
				       .body(ApiResponse.success(null));
	}
}
