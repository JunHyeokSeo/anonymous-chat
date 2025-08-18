package com.anonymouschat.anonymouschatserver.web.api.dto;

import com.anonymouschat.anonymouschatserver.application.dto.AuthUseCaseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

public class AuthDto {
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Builder
	public record AuthResponse(
			String accessToken,
			String refreshToken,
			Boolean isNewUser
	) {
		public static AuthResponse from(AuthUseCaseDto.AuthResult result) {
			return AuthResponse.builder()
					       .accessToken(result.accessToken())
					       .refreshToken(result.refreshToken())
					       .isNewUser(result.isNewUser())
					       .build();
		}
	}
}