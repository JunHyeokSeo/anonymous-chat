package com.anonymouschat.anonymouschatserver.web.api.user.dto;

import com.anonymouschat.anonymouschatserver.application.dto.UpdateUserCommand;
import com.anonymouschat.anonymouschatserver.domain.user.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.type.Region;

public record UpdateUserRequest(
		String nickname,
		Gender gender,
		int age,
		Region region,
		String bio
) {
	public UpdateUserCommand toCommand(Long userId) {
		return UpdateUserCommand.builder()
				       .id(userId)
				       .nickname(nickname)
				       .gender(gender)
				       .age(age)
				       .region(region)
				       .bio(bio)
				       .build();
	}
}
