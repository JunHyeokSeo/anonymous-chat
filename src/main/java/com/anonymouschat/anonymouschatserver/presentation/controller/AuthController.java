package com.anonymouschat.anonymouschatserver.presentation.controller;

import com.anonymouschat.anonymouschatserver.application.usecase.AuthUseCase;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.infra.web.HttpRequestExtractor;
import com.anonymouschat.anonymouschatserver.presentation.CommonResponse;
import com.anonymouschat.anonymouschatserver.presentation.controller.dto.AuthDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

	private final AuthUseCase authUseCase;
	private final HttpRequestExtractor httpRequestExtractor;

	@PostMapping("/refresh")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(summary = "토큰 재발급", description = "저장된 Refresh Token을 사용하여 Access Token을 재발급합니다.")
	@ApiResponse(ref = "Success")
	@ApiResponse(ref = "Unauthorized")
	public ResponseEntity<CommonResponse<AuthDto.AuthTokensResponse>> refresh(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal,
			HttpServletRequest httpRequest
	) {
		String userAgent = httpRequestExtractor.extractUserAgent(httpRequest);
		String ipAddress = httpRequestExtractor.extractClientIpAddress(httpRequest);
		var tokens = authUseCase.refreshByUserId(principal.userId(), userAgent, ipAddress);
		return ResponseEntity.ok(CommonResponse.success(AuthDto.AuthTokensResponse.from(tokens)));
	}

	@PostMapping("/logout")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(summary = "로그아웃", description = "Refresh Token을 무효화합니다.")
	@ApiResponse(ref = "Success")
	@ApiResponse(ref = "Unauthorized")
	public ResponseEntity<CommonResponse<Void>> logout(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal,
			HttpServletRequest httpRequest
	) {
		String accessToken = httpRequestExtractor.extractAccessToken(httpRequest);
		authUseCase.logout(principal.userId(), accessToken);
		return ResponseEntity.ok(CommonResponse.success(null));
	}
}
