package com.anonymouschat.anonymouschatserver.application.dto;

import com.anonymouschat.anonymouschatserver.domain.user.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record UpdateUserResult(
		Long id,
		String nickname,
		Gender gender,
		int age,
		Region region,
		String bio,
		LocalDateTime createdAt,
		List<UserProfileImageDto> profileImages
) {
	public static UpdateUserResult from(User user, List<UserProfileImage> images) {
		return UpdateUserResult.builder()
				       .id(user.getId())
				       .nickname(user.getNickname())
				       .gender(user.getGender())
				       .age(user.getAge())
				       .region(user.getRegion())
				       .bio(user.getBio())
				       .createdAt(user.getCreatedAt())
				       .profileImages(images.stream().map(UserProfileImageDto::from).toList())
				       .build();
	}
}
