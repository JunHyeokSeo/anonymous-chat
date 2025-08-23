package com.anonymouschat.anonymouschatserver.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Builder;

public class AuthUseCaseDto {

	@Builder
	public record AuthResult(
			String accessToken,
			@Nullable String refreshToken,
			boolean isGuestUser,
			@Nullable Long userId,
			@Nullable String userNickname
	) {}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Builder
	public record AuthTokens(
			String accessToken
	) {}

	@Builder
	public record OAuthTempData(
			String accessToken,
			String refreshToken,
			boolean isGuestUser,
			Long userId,
			String userNickname
	) {}
}