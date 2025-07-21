package com.anonymouschat.anonymouschatserver.application.usecase.user;

import com.anonymouschat.anonymouschatserver.application.service.dto.GetMyProfileServiceResult;
import com.anonymouschat.anonymouschatserver.application.service.user.UserService;
import com.anonymouschat.anonymouschatserver.application.usecase.user.dto.GetMyProfileUseCaseResult;
import com.anonymouschat.anonymouschatserver.common.annotation.UseCase;
import com.anonymouschat.anonymouschatserver.domain.user.OAuthProvider;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetMyProfileUseCase {
	private final UserService userService;

	public GetMyProfileUseCaseResult getMyProfile (OAuthProvider provider, String providerId) {
		GetMyProfileServiceResult myProfile = userService.getMyProfile(provider, providerId);
		return GetMyProfileUseCaseResult.from(myProfile);
	}
}
