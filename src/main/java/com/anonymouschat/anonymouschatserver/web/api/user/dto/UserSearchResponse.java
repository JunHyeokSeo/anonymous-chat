package com.anonymouschat.anonymouschatserver.web.api.user.dto;

public record UserSearchResponse(
		Long userId,
		String nickname,
		int age,
		String region,
		String profileImageUrl,
		String lastActiveDisplay
) {}
