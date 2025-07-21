package com.anonymouschat.anonymouschatserver.application.service.dto;

import com.anonymouschat.anonymouschatserver.domain.user.UserProfileImage;

public record GetMyProfileImageServiceResult(
		Long id,
		String imageUrl,
		boolean isRepresentative
) {
	public static GetMyProfileImageServiceResult from(UserProfileImage image) {
		return new GetMyProfileImageServiceResult(
				image.getId(),
				image.getImageUrl(),
				image.isRepresentative()
		);
	}
}
