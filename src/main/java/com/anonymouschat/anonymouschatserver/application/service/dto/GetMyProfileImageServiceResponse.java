package com.anonymouschat.anonymouschatserver.application.service.dto;

import com.anonymouschat.anonymouschatserver.domain.user.UserProfileImage;

public record GetMyProfileImageServiceResponse(
		Long id,
		String imageUrl,
		boolean isRepresentative
) {
	public static GetMyProfileImageServiceResponse from(UserProfileImage image) {
		return new GetMyProfileImageServiceResponse(
				image.getId(),
				image.getImageUrl(),
				image.isRepresentative()
		);
	}
}
