package com.anonymouschat.anonymouschatserver.application.dto;

import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import lombok.Builder;

@Builder
public record UpdateUserCommand(
		Long id,
		String nickname,
		Gender gender,
		int age,
		Region region,
		String bio
) {}
