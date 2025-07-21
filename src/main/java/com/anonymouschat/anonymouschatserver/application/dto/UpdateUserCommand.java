package com.anonymouschat.anonymouschatserver.application.dto;

import com.anonymouschat.anonymouschatserver.domain.user.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.Region;

public record UpdateUserCommand(
		Long id,
		String nickname,
		Gender gender,
		int age,
		Region region,
		String bio
) {}
