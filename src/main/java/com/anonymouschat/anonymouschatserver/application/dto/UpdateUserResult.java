package com.anonymouschat.anonymouschatserver.application.dto;

import com.anonymouschat.anonymouschatserver.domain.user.entity.User;
import com.anonymouschat.anonymouschatserver.domain.user.entity.UserProfileImage;
import com.anonymouschat.anonymouschatserver.domain.user.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.type.Region;
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
		LocalDateTime lastActiveAt,
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
				       .lastActiveAt(user.getLastActiveAt())
				       .profileImages(images.stream().map(UserProfileImageDto::from).toList())
				       .build();
	}
}
