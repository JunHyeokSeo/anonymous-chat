package com.anonymouschat.anonymouschatserver.web.api.user.dto;

import com.anonymouschat.anonymouschatserver.application.usecase.user.dto.GetMyProfileImageUseCaseResult;

public record GetMyProfileImageApiResponse(
		Long id,
		String imageUrl,
		boolean isRepresentative
) {
	public static GetMyProfileImageApiResponse from(GetMyProfileImageUseCaseResult response) {
		return new GetMyProfileImageApiResponse(
				response.id(),
				response.imageUrl(),
				response.isRepresentative()
		);
	}
}
