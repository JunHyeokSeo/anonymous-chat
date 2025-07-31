package com.anonymouschat.anonymouschatserver.application.dto;

import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

@QueryProjection
public record UserSearchResult(
		Long userId,
		String nickname,
		Gender gender,
		int age,
		Region region,
		String profileImageUrl,
		LocalDateTime lastActiveAt
) {}

