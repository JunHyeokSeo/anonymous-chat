package com.anonymouschat.anonymouschatserver.web.api.controller.test;

import com.anonymouschat.anonymouschatserver.domain.type.Role;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtTokenProvider;
import com.anonymouschat.anonymouschatserver.web.CommonResponse;
import com.anonymouschat.anonymouschatserver.web.api.dto.AuthDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Profile({"local", "dev", "test"})
@Tag(name = "Auth (Test Only)", description = "테스트용 JWT 발급 API (운영에서는 비활성화)")
public class TestAuthController {

	private final JwtTokenProvider jwtProvider;

	@PostMapping("/test-token")
	@Operation(
			summary = "테스트용 JWT 발급",
			description = "userId를 입력하면 AccessToken(JWT)을 발급합니다. (로컬/개발/테스트 환경에서만 사용 가능)",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "JWT 발급 성공",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = CommonResponse.class),
									examples = @ExampleObject(
											name = "JWT 발급 성공 예시",
											value = """
                        {
                          "success": true,
                          "data": {
                            "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                            "refreshToken": null
                          },
                          "error": null
                        }
                        """
									)
							)
					)
			}
	)
	public CommonResponse<AuthDto.AuthTokensResponse> issueTestToken(
			@RequestParam Long userId
	) {
		String accessToken = jwtProvider.createAccessToken(userId, Role.USER);
		return CommonResponse.success(new AuthDto.AuthTokensResponse(accessToken, null));
	}
}
