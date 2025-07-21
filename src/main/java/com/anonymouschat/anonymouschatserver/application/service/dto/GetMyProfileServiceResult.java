package com.anonymouschat.anonymouschatserver.application.service.dto;

import com.anonymouschat.anonymouschatserver.domain.user.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GetMyProfileServiceResult(
		Long id,
		OAuthProvider provider,
		String providerId,
		String nickname,
		Gender gender,
		int age,
		Region region,
		String bio,
		LocalDateTime createdAt,
		List<GetMyProfileImageServiceResult> profileImages
) {
	public static GetMyProfileServiceResult from(User user, List<UserProfileImage> images) {
		return GetMyProfileServiceResult.builder()
				       .id(user.getId())
				       .provider(user.getProvider())
				       .providerId(user.getProviderId())
				       .nickname(user.getNickname())
				       .gender(user.getGender())
				       .age(user.getAge())
				       .region(user.getRegion())
				       .bio(user.getBio())
				       .createdAt(user.getCreatedAt())
				       .profileImages(images.stream().map(GetMyProfileImageServiceResult::from).toList())
				       .build();
	}
}
