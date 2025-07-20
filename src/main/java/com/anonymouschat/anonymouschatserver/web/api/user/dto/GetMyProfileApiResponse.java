package com.anonymouschat.anonymouschatserver.web.api.user.dto;

import com.anonymouschat.anonymouschatserver.application.usecase.user.dto.GetMyProfileImageUseCaseResponse;
import com.anonymouschat.anonymouschatserver.application.usecase.user.dto.GetMyProfileUseCaseResponse;
import com.anonymouschat.anonymouschatserver.domain.user.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.user.Region;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GetMyProfileApiResponse(
		Long id,
		OAuthProvider provider,
		String providerId,
		String nickname,
		Gender gender,
		int age,
		Region region,
		String bio,
		LocalDateTime createdAt,
		List<GetMyProfileImageApiResponse> profileImages
) {
	public static GetMyProfileApiResponse from(GetMyProfileUseCaseResponse response) {
		List<GetMyProfileImageUseCaseResponse> images = response.profileImages();
		return GetMyProfileApiResponse.builder()
				       .id(response.id())
				       .provider(response.provider())
				       .providerId(response.providerId())
				       .nickname(response.nickname())
				       .gender(response.gender())
				       .age(response.age())
				       .region(response.region())
				       .bio(response.bio())
				       .createdAt(response.createdAt())
				       .profileImages(images.stream().map(GetMyProfileImageApiResponse::from).toList())
				       .build();
	}
}
