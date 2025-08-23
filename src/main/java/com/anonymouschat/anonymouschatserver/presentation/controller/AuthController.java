package com.anonymouschat.anonymouschatserver.presentation.controller;

import com.anonymouschat.anonymouschatserver.application.usecase.AuthUseCase;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.infra.web.HttpRequestExtractor;
import com.anonymouschat.anonymouschatserver.presentation.CommonResponse;
import com.anonymouschat.anonymouschatserver.presentation.controller.dto.AuthDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
	@Operation(
			summary = "토큰 재발급",
			description = "현재 사용자의 저장된 Refresh Token을 사용하여 Access Token을 재발급합니다.",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "재발급 성공",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = CommonResponse.class),
									examples = @ExampleObject(
											name = "재발급 성공 예시",
											value = """
                    {
                      "success": true,
                      "data": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                      },
                      "error": null
                    }
                    """
									)
							)
					),
					@ApiResponse(responseCode = "401", description = "인증 실패 또는 Refresh Token 없음")
			}
	)
	public ResponseEntity<CommonResponse<AuthDto.AuthTokensResponse>> refresh(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal,
			HttpServletRequest httpRequest
	) {
		String userAgent = httpRequestExtractor.extractUserAgent(httpRequest);
		String ipAddress = httpRequestExtractor.extractClientIpAddress(httpRequest);

		var tokens = authUseCase.refreshByUserId(principal.userId(), userAgent, ipAddress);

		return ResponseEntity
				       .status(HttpStatus.OK)
				       .body(CommonResponse.success(AuthDto.AuthTokensResponse.from(tokens)));
	}

	@PostMapping("/logout")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(
			summary = "로그아웃",
			description = "현재 로그인된 사용자의 Refresh Token을 무효화합니다. (USER, ADMIN 권한 필요)",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "로그아웃 성공",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = CommonResponse.class),
									examples = @ExampleObject(
											name = "로그아웃 성공 예시",
											value = """
                        {
                          "success": true,
                          "data": null,
                          "error": null
                        }
                        """
									)
							)
					),
					@ApiResponse(responseCode = "401", description = "인증 실패")
			}
	)
	public ResponseEntity<CommonResponse<Void>> logout(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal principal,
			HttpServletRequest httpRequest
	) {
		String accessToken = httpRequestExtractor.extractAccessToken(httpRequest);
		authUseCase.logout(principal.userId(), accessToken);

		return ResponseEntity
				       .status(HttpStatus.OK)
				       .body(CommonResponse.success(null));
	}
}