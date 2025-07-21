package com.anonymouschat.anonymouschatserver.application.dto;

import com.anonymouschat.anonymouschatserver.domain.user.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.user.type.Region;
import com.anonymouschat.anonymouschatserver.domain.user.entity.User;

public record RegisterUserCommand(
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
