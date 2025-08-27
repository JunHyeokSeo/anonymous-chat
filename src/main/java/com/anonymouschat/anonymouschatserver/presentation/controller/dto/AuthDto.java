package com.anonymouschat.anonymouschatserver.presentation.controller.dto;

import com.anonymouschat.anonymouschatserver.application.dto.AuthUseCaseDto;
import io.swagger.v3.oas.annotations.media.Schema;

public class AuthDto {
	public record AuthTokensResponse(
			@Schema(description = "새로 발급된 Access Token", example = "eyJhbGciOiJIUzI1...")
			String accessToken
	) {
		public static AuthTokensResponse from(AuthUseCaseDto.AuthTokens result) {
			return new AuthTokensResponse(
					result.accessToken()
			);
		}
	}
}
