package com.anonymouschat.anonymouschatserver.web.controller.dto;

import com.anonymouschat.anonymouschatserver.application.dto.UserUseCaseDto;
import com.anonymouschat.anonymouschatserver.common.validation.ValidAgeRange;
import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.LocalDateTime;

public class UserDto {

	@Schema(description = "회원가입 요청 DTO")
	public record RegisterRequest(
			@Schema(description = "닉네임 (2~20자)", example = "홍길동")
			@NotBlank(message = "닉네임은 필수입니다.")
			@Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
			String nickname,

			@Schema(description = "성별", example = "MALE")
			@NotNull(message = "성별은 필수입니다.")
			Gender gender,

			@Schema(description = "나이 (0~100)", example = "27")
			@Min(value = 0, message = "나이는 0세 이상이어야 합니다.")
			@Max(value = 100, message = "나이는 100세 이하여야 합니다.")
			int age,

			@Schema(description = "지역", example = "SEOUL")
			@NotNull(message = "지역은 필수입니다.")
			Region region,

			@Schema(description = "자기소개 (최대 200자)", example = "안녕하세요! 백엔드 개발자입니다.")
			@Size(max = 200, message = "자기소개는 최대 200자까지 입력 가능합니다.")
			String bio
	) {}

	@Schema(description = "회원가입 응답 DTO")
	@Builder
	public record RegisterResponse(
			@Schema(description = "ROLE_USER accessToken, 세션 스토리지 저장용")
			String accessToken
	) {}

	@Schema(description = "회원정보 수정 요청 DTO")
	public record UpdateRequest(
			@Schema(description = "닉네임 (2~20자)", example = "김철수")
			@NotBlank(message = "닉네임은 필수입니다.")
			@Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
			String nickname,

			@Schema(description = "성별", example = "FEMALE")
			@NotNull(message = "성별은 필수입니다.")
			Gender gender,

			@Schema(description = "나이 (0~100)", example = "25")
			@Min(value = 0, message = "나이는 0세 이상이어야 합니다.")
			@Max(value = 100, message = "나이는 100세 이하여야 합니다.")
			int age,

			@Schema(description = "지역", example = "BUSAN")
			@NotNull(message = "지역은 필수입니다.")
			Region region,

			@Schema(description = "자기소개 (최대 200자)", example = "안녕하세요! 반갑습니다 :)")
			@Size(max = 200, message = "자기소개는 최대 200자까지 입력 가능합니다.")
			String bio
	) {}

	@Schema(description = "내 프로필 응답 DTO")
	public record ProfileResponse(
			@Schema(description = "유저 ID", example = "101")
			Long id,

			@Schema(description = "닉네임", example = "홍길동")
			String nickname,

			@Schema(description = "성별", example = "MALE")
			Gender gender,

			@Schema(description = "나이", example = "27")
			int age,

			@Schema(description = "지역", example = "SEOUL")
			Region region,

			@Schema(description = "자기소개", example = "안녕하세요! 백엔드 개발자입니다.")
			String bio,

			@Schema(description = "가입일", example = "2025-08-18T10:15:30")
			LocalDateTime createdAt,

			@Schema(description = "마지막 활동 시각", example = "2025-08-19T09:00:00")
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

	@Schema(description = "유저 검색 조건 요청 DTO")
	@ValidAgeRange
	public record SearchConditionRequest(
			@Schema(description = "성별", example = "MALE")
			Gender gender,

			@Schema(description = "최소 나이", example = "20")
			@Min(value = 0, message = "최소 나이는 0세 이상이어야 합니다.")
			@Max(value = 100, message = "최소 나이는 100세 이하여야 합니다.")
			Integer minAge,

			@Schema(description = "최대 나이", example = "30")
			@Min(value = 0, message = "최대 나이는 0세 이상이어야 합니다.")
			@Max(value = 100, message = "최대 나이는 100세 이하여야 합니다.")
			Integer maxAge,

			@Schema(description = "지역", example = "SEOUL")
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

	@Schema(description = "유저 검색 응답 DTO")
	public record SearchResponse(
			@Schema(description = "유저 ID", example = "201")
			Long userId,

			@Schema(description = "닉네임", example = "김철수")
			String nickname,

			@Schema(description = "성별", example = "MALE")
			Gender gender,

			@Schema(description = "나이", example = "29")
			int age,

			@Schema(description = "지역", example = "BUSAN")
			Region region,

			@Schema(description = "프로필 이미지 URL", example = "https://example.com/images/201.png")
			String profileImageUrl,

			@Schema(description = "마지막 활동 시각", example = "2025-08-18T22:00:00")
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
