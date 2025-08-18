package com.anonymouschat.anonymouschatserver.application.dto;

import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.entity.UserProfileImage;
import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class UserServiceDto {

	public record RegisterCommand(
			String nickname,
			Gender gender,
			int age,
			Region region,
			String bio,
			OAuthProvider provider,
			String providerId
	) {
		public static RegisterCommand from(UserUseCaseDto.RegisterRequest dto) {
			return new RegisterCommand(
					dto.nickname(),
					dto.gender(),
					dto.age(),
					dto.region(),
					dto.bio(),
					dto.provider(),
					dto.providerId()
			);
		}
	}

	public record UpdateCommand(
			Long userId,
			String nickname,
			Gender gender,
			int age,
			Region region,
			String bio
	) {
		public static UpdateCommand from(UserUseCaseDto.UpdateRequest dto) {
			return new UpdateCommand(
					dto.id(),
					dto.nickname(),
					dto.gender(),
					dto.age(),
					dto.region(),
					dto.bio()
			);
		}
	}

	@Builder
	public record SearchCommand(
			Long id,
			Gender gender,
			Integer minAge,
			Integer maxAge,
			Region region,
			List<Long> blockedUserIds
	) {
		public static SearchCommand from(UserUseCaseDto.SearchConditionRequest dto, List<Long> blockedUserIds) {
			return SearchCommand.builder()
					       .id(dto.id())
					       .gender(dto.gender())
					       .minAge(dto.minAge())
					       .maxAge(dto.maxAge())
					       .region(dto.region())
					       .blockedUserIds(blockedUserIds)
					       .build();
		}
	}

	@Builder
	public record ProfileResult(
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
		public static ProfileResult from(User user, List<UserProfileImage> images) {
			return ProfileResult.builder()
					       .id(user.getId())
					       .nickname(user.getNickname())
					       .gender(user.getGender())
					       .age(user.getAge())
					       .region(user.getRegion())
					       .bio(user.getBio())
					       .createdAt(user.getCreatedAt())
					       .lastActiveAt(user.getLastActiveAt())
					       .profileImages(images.stream().map(UserProfileImageDto::from).toList())
					       .build();
		}
	}

	@Builder
	public record SearchResult(
			Long userId,
			String nickname,
			Gender gender,
			int age,
			Region region,
			String profileImageUrl,
			LocalDateTime lastActiveAt
	) {
		@QueryProjection
		public SearchResult {}
	}
}
