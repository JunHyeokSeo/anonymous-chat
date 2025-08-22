package com.anonymouschat.anonymouschatserver.application.dto;

import lombok.Builder;

@Builder
public record OAuthTokenData(
		String accessToken,
		String refreshToken,
		boolean isGuestUser,
		Long userId,
		String userNickname
) {}