package com.anonymouschat.anonymouschatserver.application.dto;

import com.anonymouschat.anonymouschatserver.domain.user.UserProfileImage;

public record GetMyProfileImageResult(
		Long id,
		String imageUrl,
		boolean isRepresentative
) {
	public static GetMyProfileImageResult from(UserProfileImage image) {
		return new GetMyProfileImageResult(
				image.getId(),
				image.getImageUrl(),
				image.isRepresentative()
		);
	}
}
