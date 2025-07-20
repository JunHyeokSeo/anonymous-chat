package com.anonymouschat.anonymouschatserver.application.usecase.user.dto;

import com.anonymouschat.anonymouschatserver.application.service.dto.RegisterUserServiceRequest;
import com.anonymouschat.anonymouschatserver.domain.user.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.user.Region;
import lombok.Builder;

public record RegisterUserCommand(
		String nickname,
		Gender gender,
		int age,
		Region region,
		String bio,
		OAuthProvider provider,
		String providerId
) {
	public RegisterUserServiceRequest toServiceRequest() {
		return new RegisterUserServiceRequest(
			nickname, gender, age, region, bio, provider, providerId
		);
	}
}
