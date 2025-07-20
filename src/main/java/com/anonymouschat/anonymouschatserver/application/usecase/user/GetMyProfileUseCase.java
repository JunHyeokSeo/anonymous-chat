package com.anonymouschat.anonymouschatserver.application.usecase.user;

import com.anonymouschat.anonymouschatserver.application.service.dto.GetMyProfileServiceResponse;
import com.anonymouschat.anonymouschatserver.application.service.user.UserService;
import com.anonymouschat.anonymouschatserver.application.usecase.user.dto.GetMyProfileUseCaseResponse;
import com.anonymouschat.anonymouschatserver.common.annotation.UseCase;
import com.anonymouschat.anonymouschatserver.domain.user.OAuthProvider;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetMyProfileUseCase {
	private final UserService userService;

	public GetMyProfileUseCaseResponse getMyProfile (OAuthProvider provider, String providerId) {
		GetMyProfileServiceResponse myProfile = userService.getMyProfile(provider, providerId);
		return GetMyProfileUseCaseResponse.from(myProfile);
	}
}
