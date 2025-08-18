package com.anonymouschat.anonymouschatserver.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Builder;

public class AuthUseCaseDto {
	@Builder
	public record AuthResult(
			String accessToken,
			@Nullable String refreshToken,
			boolean isNewUser
	) {}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Builder
	public record AuthTokens(
			String accessToken,
			@Nullable String refreshToken
	) {}
}
