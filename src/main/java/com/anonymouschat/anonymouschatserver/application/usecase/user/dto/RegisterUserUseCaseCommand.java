package com.anonymouschat.anonymouschatserver.application.usecase.user.dto;

import com.anonymouschat.anonymouschatserver.application.service.dto.RegisterUserServiceCommand;
import com.anonymouschat.anonymouschatserver.domain.user.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.user.Region;

public record RegisterUserUseCaseCommand(
		String nickname,
		Gender gender,
		int age,
		Region region,
		String bio,
		OAuthProvider provider,
		String providerId
) {
	public RegisterUserServiceCommand toServiceRequest() {
		return new RegisterUserServiceCommand(
			nickname, gender, age, region, bio, provider, providerId
		);
	}
}
