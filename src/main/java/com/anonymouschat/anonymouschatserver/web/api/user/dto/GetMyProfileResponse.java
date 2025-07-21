package com.anonymouschat.anonymouschatserver.web.api.user.dto;

import com.anonymouschat.anonymouschatserver.application.dto.UserProfileImageDto;
import com.anonymouschat.anonymouschatserver.application.dto.GetMyProfileResult;
import com.anonymouschat.anonymouschatserver.domain.user.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.type.Region;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GetMyProfileResponse(
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
	public static GetMyProfileResponse from(GetMyProfileResult response) {
		return GetMyProfileResponse.builder()
				       .id(response.id())
				       .nickname(response.nickname())
				       .gender(response.gender())
				       .age(response.age())
				       .region(response.region())
				       .bio(response.bio())
				       .createdAt(response.createdAt())
				       .lastActiveAt(response.lastActiveAt())
				       .profileImages(response.profileImages())
				       .build();
	}
}
