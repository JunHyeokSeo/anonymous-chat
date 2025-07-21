package com.anonymouschat.anonymouschatserver.application.usecase.user;

import com.anonymouschat.anonymouschatserver.application.dto.GetMyProfileResult;
import com.anonymouschat.anonymouschatserver.application.service.user.UserService;
import com.anonymouschat.anonymouschatserver.application.dto.GetMyProfileResult;
import com.anonymouschat.anonymouschatserver.common.annotation.UseCase;
import com.anonymouschat.anonymouschatserver.domain.user.OAuthProvider;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetMyProfileUseCase {
	private final UserService userService;

	public GetMyProfileResult getMyProfile (OAuthProvider provider, String providerId) {
		return userService.getMyProfile(provider, providerId);
	}
}
