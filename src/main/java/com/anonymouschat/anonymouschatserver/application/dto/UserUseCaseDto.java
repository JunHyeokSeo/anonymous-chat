package com.anonymouschat.anonymouschatserver.application.dto;

import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class UserUseCaseDto {
	public record RegisterRequest(
			String nickname,
			Gender gender,
			int age,
			Region region,
			String bio,
			OAuthProvider provider,
			String providerId
	) {}

	public record UpdateRequest(
			Long id,
			String nickname,
			Gender gender,
			int age,
			Region region,
			String bio
	) {}

	@Builder
	public record ProfileResponse(
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
		public static ProfileResponse from(UserServiceDto.ProfileResult result) {
			return ProfileResponse.builder()
					       .id(result.id())
					       .nickname(result.nickname())
					       .gender(result.gender())
					       .age(result.age())
					       .region(result.region())
					       .bio(result.bio())
					       .createdAt(result.createdAt())
					       .lastActiveAt(result.lastActiveAt())
					       .profileImages(result.profileImages())
					       .build();
		}
	}

	@Builder
	public record SearchConditionRequest(
			Long id,
			Gender gender,
			Integer minAge,
			Integer maxAge,
			Region region
	) {}

	@Builder
	public record SearchResponse(
			Long userId,
			String nickname,
			Gender gender,
			int age,
			Region region,
			String profileImageUrl,
			LocalDateTime lastActiveAt
	) {
		public static SearchResponse from(UserServiceDto.SearchResult result) {
			return SearchResponse.builder()
					       .userId(result.userId())
					       .nickname(result.nickname())
					       .gender(result.gender())
					       .age(result.age())
					       .region(result.region())
					       .profileImageUrl(result.profileImageUrl())
					       .lastActiveAt(result.lastActiveAt())
					       .build();
		}
	}
}
