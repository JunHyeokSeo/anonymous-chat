package com.anonymouschat.anonymouschatserver.application.dto;

import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.entity.UserProfileImage;
import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GetMyProfileResult(
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
	public static GetMyProfileResult from(User user, List<UserProfileImage> images) {
		return GetMyProfileResult.builder()
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
