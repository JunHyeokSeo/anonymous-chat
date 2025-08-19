package com.anonymouschat.anonymouschatserver.web.controller.dto;

import com.anonymouschat.anonymouschatserver.application.dto.AuthUseCaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class AuthDto {
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
