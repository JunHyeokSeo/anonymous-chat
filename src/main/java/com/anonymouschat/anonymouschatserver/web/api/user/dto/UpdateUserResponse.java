package com.anonymouschat.anonymouschatserver.web.api.user.dto;

import com.anonymouschat.anonymouschatserver.application.dto.UpdateUserResult;
import com.anonymouschat.anonymouschatserver.application.dto.UserProfileImageDto;
import com.anonymouschat.anonymouschatserver.domain.user.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.Region;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record UpdateUserResponse(
		Long id,
		String nickname,
		Gender gender,
		int age,
		Region region,
		String bio,
		LocalDateTime createdAt,
		List<UserProfileImageDto> profileImages
) {
	public static UpdateUserResponse from(UpdateUserResult result) {
		return UpdateUserResponse.builder()
				       .id(result.id())
				       .nickname(result.nickname())
				       .gender(result.gender())
				       .age(result.age())
				       .region(result.region())
				       .bio(result.bio())
				       .createdAt(result.createdAt())
				       .profileImages(result.profileImages())
				       .build();
	}
}
