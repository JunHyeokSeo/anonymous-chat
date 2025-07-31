package com.anonymouschat.anonymouschatserver.web.api.user.dto;

import com.anonymouschat.anonymouschatserver.application.dto.UserUseCaseDto;
import lombok.Builder;

@Builder
public record UserSearchResponse(
		Long userId,
		String nickname,
		int age,
		String region,
		String profileImageUrl,
		String lastActiveDisplay
) {
	public static UserSearchResponse from(UserUseCaseDto.SearchResult result) {
		return UserSearchResponse.builder()
				       .userId(result.userId())
				       .nickname(result.nickname())
				       .age(result.age())
				       .region(String.valueOf(result.region()))
				       .profileImageUrl(result.profileImageUrl())
				       //todo: 해당 부분 화면에 출력되는 형태로 바꿔야 함 util 생성?
				       .lastActiveDisplay(String.valueOf(result.lastActiveAt()))
				       .build();
	}
}
