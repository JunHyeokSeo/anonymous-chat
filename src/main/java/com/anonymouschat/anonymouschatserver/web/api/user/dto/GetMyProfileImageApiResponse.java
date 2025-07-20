package com.anonymouschat.anonymouschatserver.web.api.user.dto;

import com.anonymouschat.anonymouschatserver.application.service.dto.GetMyProfileImageServiceResponse;
import com.anonymouschat.anonymouschatserver.application.usecase.user.dto.GetMyProfileImageUseCaseResponse;
import com.anonymouschat.anonymouschatserver.application.usecase.user.dto.GetMyProfileUseCaseResponse;

public record GetMyProfileImageApiResponse(
		Long id,
		String imageUrl,
		boolean isRepresentative
) {
	public static GetMyProfileImageApiResponse from(GetMyProfileImageUseCaseResponse response) {
		return new GetMyProfileImageApiResponse(
				response.id(),
				response.imageUrl(),
				response.isRepresentative()
		);
	}
}
