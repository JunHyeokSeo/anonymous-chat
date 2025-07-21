package com.anonymouschat.anonymouschatserver.application.usecase.user.dto;

import com.anonymouschat.anonymouschatserver.application.service.dto.GetMyProfileImageServiceResult;
import com.anonymouschat.anonymouschatserver.application.service.dto.GetMyProfileServiceResult;
import com.anonymouschat.anonymouschatserver.domain.user.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.user.Region;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GetMyProfileUseCaseResult(
		Long id,
		OAuthProvider provider,
		String providerId,
		String nickname,
		Gender gender,
		int age,
		Region region,
		String bio,
		LocalDateTime createdAt,
		List<GetMyProfileImageUseCaseResult> profileImages
) {
	public static GetMyProfileUseCaseResult from(GetMyProfileServiceResult response) {
		List<GetMyProfileImageServiceResult> images = response.profileImages();
		return GetMyProfileUseCaseResult.builder()
				       .id(response.id())
				       .provider(response.provider())
				       .providerId(response.providerId())
				       .nickname(response.nickname())
				       .gender(response.gender())
				       .age(response.age())
				       .region(response.region())
				       .bio(response.bio())
				       .createdAt(response.createdAt())
				       .profileImages(images.stream().map(GetMyProfileImageUseCaseResult::from).toList())
				       .build();
	}
}
