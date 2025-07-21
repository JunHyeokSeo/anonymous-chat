package com.anonymouschat.anonymouschatserver.application.dto;

import com.anonymouschat.anonymouschatserver.domain.user.entity.UserProfileImage;

public record UserProfileImageDto(
		Long id,
		String imageUrl,
		boolean isRepresentative
) {
	public static UserProfileImageDto from(UserProfileImage image) {
		return new UserProfileImageDto(
				image.getId(),
				image.getImageUrl(),
				image.isRepresentative()
		);
	}
}
