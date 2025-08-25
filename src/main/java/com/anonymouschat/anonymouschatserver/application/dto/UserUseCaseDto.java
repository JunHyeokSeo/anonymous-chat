package com.anonymouschat.anonymouschatserver.application.dto;

import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import com.anonymouschat.anonymouschatserver.domain.type.Role;
import com.anonymouschat.anonymouschatserver.presentation.controller.dto.UserDto;
import lombok.Builder;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

public class UserUseCaseDto {
	@Builder
	public record RegisterRequest(
			Long userId,
			String nickname,
			Gender gender,
			int age,
			Region region,
			String bio
	) {
		public static RegisterRequest from(UserDto.RegisterRequest request, Long userId) {
			return RegisterRequest.builder()
					       .userId(userId)
					       .nickname(request.nickname())
					       .gender(request.gender())
					       .age(request.age())
					       .region(request.region())
					       .bio(request.bio() == null ? "" : request.bio())
					       .build();
		}
	}

	@Builder
	public record RegisterResponse(
			Long userId,
			OAuthProvider provider,
			String providerId,
			String nickname,
			Role role,
			Gender gender,
			int age,
			Region region,
			String bio
	) {
		public static RegisterResponse from(User user) {
			return RegisterResponse.builder()
					       .userId(user.getId())
					       .provider(user.getProvider())
					       .providerId(user.getProviderId())
					       .nickname(user.getNickname())
					       .role(user.getRole())
					       .gender(user.getGender())
					       .age(user.getAge())
					       .region(user.getRegion())
					       .bio(user.getBio())
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
		public static UpdateRequest from(UserDto.UpdateRequest request, Long userId) {
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
			Instant createdAt,
			Instant lastActiveAt,
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
					       .createdAt(result.createdAt().toInstant(ZoneOffset.UTC))
					       .lastActiveAt(result.lastActiveAt().toInstant(ZoneOffset.UTC))
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
		public static SearchConditionRequest from(UserDto.SearchConditionRequest request, Long userId) {
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
			Instant lastActiveAt
	) {
		public static SearchResponse from(UserServiceDto.SearchResult result) {
			return SearchResponse.builder()
					       .userId(result.userId())
					       .nickname(result.nickname())
					       .gender(result.gender())
					       .age(result.age())
					       .region(result.region())
					       .profileImageUrl(result.profileImageUrl())
					       .lastActiveAt(result.lastActiveAt().toInstant(ZoneOffset.UTC))
					       .build();
		}
	}
}
