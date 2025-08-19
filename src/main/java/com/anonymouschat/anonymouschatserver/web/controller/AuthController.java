package com.anonymouschat.anonymouschatserver.web.controller;

import com.anonymouschat.anonymouschatserver.application.usecase.AuthUseCase;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.web.CommonResponse;
import com.anonymouschat.anonymouschatserver.web.controller.dto.AuthDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

	private final AuthUseCase authUseCase;

	@PostMapping("/refresh")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	@Operation(
			summary = "토큰 재발급",
			description = "Refresh Token을 사용하여 Access Token을 재발급합니다. (USER, ADMIN 권한 필요)",
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
                            "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                            "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                          },
                          "error": null
                        }
                        """
									)
							)
					),
					@ApiResponse(responseCode = "400", description = "유효하지 않은 요청"),
					@ApiResponse(responseCode = "401", description = "인증 실패")
			}
	)
	public ResponseEntity<CommonResponse<AuthDto.AuthTokensResponse>> refresh(
			@Valid @RequestBody AuthDto.RefreshRequest request
	) {
		var tokens = authUseCase.refresh(request.refreshToken());
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
			@Parameter(hidden = true)
			@AuthenticationPrincipal CustomPrincipal principal
	) {
		authUseCase.logout(principal.userId());
		return ResponseEntity
				       .status(HttpStatus.OK)
				       .body(CommonResponse.success(null));
	}
}
