package com.anonymouschat.anonymouschatserver.application.usecase.user.dto;

import com.anonymouschat.anonymouschatserver.application.service.dto.GetMyProfileImageServiceResponse;
import com.anonymouschat.anonymouschatserver.application.service.dto.GetMyProfileServiceResponse;
import com.anonymouschat.anonymouschatserver.domain.user.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.user.Region;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GetMyProfileUseCaseResponse(
		Long id,
		OAuthProvider provider,
		String providerId,
		String nickname,
		Gender gender,
		int age,
		Region region,
		String bio,
		LocalDateTime createdAt,
		List<GetMyProfileImageUseCaseResponse> profileImages
) {
	public static GetMyProfileUseCaseResponse from(GetMyProfileServiceResponse response) {
		List<GetMyProfileImageServiceResponse> images = response.profileImages();
		return GetMyProfileUseCaseResponse.builder()
				       .id(response.id())
				       .provider(response.provider())
				       .providerId(response.providerId())
				       .nickname(response.nickname())
				       .gender(response.gender())
				       .age(response.age())
				       .region(response.region())
				       .bio(response.bio())
				       .createdAt(response.createdAt())
				       .profileImages(images.stream().map(GetMyProfileImageUseCaseResponse::from).toList())
				       .build();
	}
}
