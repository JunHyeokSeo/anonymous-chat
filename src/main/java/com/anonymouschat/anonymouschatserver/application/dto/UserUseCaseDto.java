package com.anonymouschat.anonymouschatserver.application.dto;

import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import com.anonymouschat.anonymouschatserver.web.api.dto.UserControllerDto;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class UserUseCaseDto {
	@Builder
	public record RegisterRequest(
			String nickname,
			Gender gender,
			int age,
			Region region,
			String bio,
			OAuthProvider provider,
			String providerId
	) {
		public static RegisterRequest from(UserControllerDto.RegisterRequest request) {
			return RegisterRequest.builder()
					       .nickname(request.nickname())
					       .gender(request.gender())
					       .age(request.age())
					       .region(request.region())
					       .bio(request.bio())
					       .provider(request.provider())
					       .providerId(request.providerId())
					       .build();
		}
	}

	@Builder
	public record UpdateRequest(
			Long id,
			String nickname,
			Gender gender,
			int age,
			Region region,
			String bio
	) {
		public static UpdateRequest from(UserControllerDto.UpdateRequest request, Long userId) {
			return UpdateRequest.builder()
					       .id(userId)
					       .nickname(request.nickname())
					       .gender(request.gender())
					       .age(request.age())
					       .region(request.region())
					       .bio(request.bio())
					       .build();
		}
	}

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
	) {
		public static SearchConditionRequest from(UserControllerDto.SearchConditionRequest request, Long userId) {
			return SearchConditionRequest.builder()
					       .id(userId)
					       .gender(request.gender())
					       .minAge(request.minAge())
					       .maxAge(request.maxAge())
					       .region(request.region())
					       .build();
		}
	}

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
