package com.anonymouschat.anonymouschatserver.application.service.dto;

import com.anonymouschat.anonymouschatserver.domain.user.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GetMyProfileServiceResponse(
		Long id,
		OAuthProvider provider,
		String providerId,
		String nickname,
		Gender gender,
		int age,
		Region region,
		String bio,
		LocalDateTime createdAt,
		List<GetMyProfileImageServiceResponse> profileImages
) {
	public static GetMyProfileServiceResponse from(User user, List<UserProfileImage> images) {
		return GetMyProfileServiceResponse.builder()
				       .id(user.getId())
				       .provider(user.getProvider())
				       .providerId(user.getProviderId())
				       .nickname(user.getNickname())
				       .gender(user.getGender())
				       .age(user.getAge())
				       .region(user.getRegion())
				       .bio(user.getBio())
				       .createdAt(user.getCreatedAt())
				       .profileImages(images.stream().map(GetMyProfileImageServiceResponse::from).toList())
				       .build();
	}
}
