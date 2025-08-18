package com.anonymouschat.anonymouschatserver.web.api.dto;

import com.anonymouschat.anonymouschatserver.application.dto.UserUseCaseDto;
import com.anonymouschat.anonymouschatserver.common.validation.ValidAgeRange;
import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class UserDto {

	public record RegisterRequest(
			@NotBlank(message = "닉네임은 필수입니다.")
			@Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
			String nickname,

			@NotNull(message = "성별은 필수입니다.")
			Gender gender,

			@Min(value = 0, message = "나이는 0세 이상이어야 합니다.")
			@Max(value = 100, message = "나이는 100세 이하여야 합니다.")
			int age,

			@NotNull(message = "지역은 필수입니다.")
			Region region,

			@Size(max = 200, message = "자기소개는 최대 200자까지 입력 가능합니다.")
			String bio
	) {}

	public record UpdateRequest(
			@NotBlank(message = "닉네임은 필수입니다.")
			@Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
			String nickname,

			@NotNull(message = "성별은 필수입니다.")
			Gender gender,

			@Min(value = 0, message = "나이는 0세 이상이어야 합니다.")
			@Max(value = 100, message = "나이는 100세 이하여야 합니다.")
			int age,

			@NotNull(message = "지역은 필수입니다.")
			Region region,

			@Size(max = 200, message = "자기소개는 최대 200자까지 입력 가능합니다.")
			String bio
	) {}

	public record ProfileResponse(
			Long id,
			String nickname,
			Gender gender,
			int age,
			Region region,
			String bio,
			LocalDateTime createdAt,
			LocalDateTime lastActiveAt
	) {
		public static ProfileResponse from(UserUseCaseDto.ProfileResponse response) {
			return new ProfileResponse(
					response.id(),
					response.nickname(),
					response.gender(),
					response.age(),
					response.region(),
					response.bio(),
					response.createdAt(),
					response.lastActiveAt()
			);
		}
	}

	@ValidAgeRange
	public record SearchConditionRequest(
			Gender gender,

			@Min(value = 0, message = "최소 나이는 0세 이상이어야 합니다.")
			@Max(value = 100, message = "최소 나이는 100세 이하여야 합니다.")
			Integer minAge,

			@Min(value = 0, message = "최대 나이는 0세 이상이어야 합니다.")
			@Max(value = 100, message = "최대 나이는 100세 이하여야 합니다.")
			Integer maxAge,

			Region region
	) {
		public static UserUseCaseDto.SearchConditionRequest from(SearchConditionRequest request, Long userId) {
			return new UserUseCaseDto.SearchConditionRequest(
					userId,
					request.gender(),
					request.minAge(),
					request.maxAge(),
					request.region()
			);
		}
	}

	public record SearchResponse(
			Long userId,
			String nickname,
			Gender gender,
			int age,
			Region region,
			String profileImageUrl,
			LocalDateTime lastActiveAt
	) {
		public static SearchResponse from(UserUseCaseDto.SearchResponse response) {
			return new SearchResponse(
					response.userId(),
					response.nickname(),
					response.gender(),
					response.age(),
					response.region(),
					response.profileImageUrl(),
					response.lastActiveAt()
			);
		}
	}
}
