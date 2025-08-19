package com.anonymouschat.anonymouschatserver.web.api.dto;

import com.anonymouschat.anonymouschatserver.application.dto.AuthUseCaseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

public class AuthDto {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Builder
	public record AuthResultResponse(
			@Schema(description = "새로운 Access Token", example = "eyJhbGciOiJIUzI1...")
			String accessToken,

			@Schema(description = "새로운 Refresh Token", example = "eyJhbGciOiJIUzI1...")
			String refreshToken,

			@Schema(description = "신규 가입자인지 여부", example = "true")
			Boolean isNewUser
	) {
		public static AuthResultResponse from(AuthUseCaseDto.AuthResult result) {
			return AuthResultResponse.builder()
					       .accessToken(result.accessToken())
					       .refreshToken(result.refreshToken())
					       .isNewUser(result.isNewUser())
					       .build();
		}
	}

	public record RefreshRequest(
			@Schema(description = "재발급을 위한 Refresh Token", example = "eyJhbGciOiJIUzI1...")
			@NotBlank(message = "refreshToken은 필수입니다.")
			String refreshToken
	) {}

	public record AuthTokensResponse(
			@Schema(description = "새로 발급된 Access Token", example = "eyJhbGciOiJIUzI1...")
			String accessToken,

			@Schema(description = "새로 발급된 Refresh Token", example = "eyJhbGciOiJIUzI1...")
			String refreshToken
	) {
		public static AuthTokensResponse from(AuthUseCaseDto.AuthTokens result) {
			return new AuthTokensResponse(
					result.accessToken(),
					result.refreshToken()
			);
		}
	}
}
