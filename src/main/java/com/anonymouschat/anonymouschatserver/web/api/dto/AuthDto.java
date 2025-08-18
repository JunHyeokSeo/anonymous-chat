package com.anonymouschat.anonymouschatserver.web.api.dto;

import com.anonymouschat.anonymouschatserver.application.dto.AuthUseCaseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

public class AuthDto {
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Builder
	public record AuthResultResponse(
			String accessToken,
			String refreshToken,
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
			@NotBlank(message = "refreshToken은 필수입니다.")
			String refreshToken
	) {}

	public record AuthTokensResponse(
			String accessToken,
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