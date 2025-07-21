package com.anonymouschat.anonymouschatserver.web.api.user.dto;

import com.anonymouschat.anonymouschatserver.application.dto.UserProfileImageDto;

public record GetMyProfileImageResponse(
		Long id,
		String imageUrl,
		boolean isRepresentative
) {
	public static GetMyProfileImageResponse from(UserProfileImageDto response) {
		return new GetMyProfileImageResponse(
				response.id(),
				response.imageUrl(),
				response.isRepresentative()
		);
	}
}
