package com.anonymouschat.anonymouschatserver.web.api.user.dto;

import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.Region;

public record UpdateUserRequest(
		String nickname,
		Gender gender,
		int age,
		Region region,
		String bio
) {
}
