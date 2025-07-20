package com.anonymouschat.anonymouschatserver.application.service.dto;

import com.anonymouschat.anonymouschatserver.domain.user.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.user.Region;
import com.anonymouschat.anonymouschatserver.domain.user.User;

public record RegisterUserServiceRequest(
		String nickname,
		Gender gender,
		int age,
		Region region,
		String bio,
		OAuthProvider provider,
		String providerId
) {
	public User toEntity() {
		return User.builder()
				       .nickname(nickname)
				       .gender(gender)
				       .age(age)
				       .region(region)
				       .bio(bio)
				       .provider(provider)
				       .providerId(providerId)
				       .build();
	}
}
