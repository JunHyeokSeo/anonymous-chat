package com.anonymouschat.anonymouschatserver.application.dto;

import lombok.Builder;

@Builder
public record AuthResult(
		String accessToken,
		String refreshToken,
		boolean isNewUser
) {}