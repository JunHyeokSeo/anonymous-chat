package com.anonymouschat.anonymouschatserver.application.service.dto;

import com.anonymouschat.anonymouschatserver.domain.user.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.user.Region;

public record RegisterUserServiceRequest(
		String nickname,
		Gender gender,
		int age,
		Region region,
		String bio,
		OAuthProvider provider,
		String providerId
) {
}
