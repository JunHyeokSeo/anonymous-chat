package com.anonymouschat.anonymouschatserver.application.usecase.user.dto;

import com.anonymouschat.anonymouschatserver.application.service.dto.GetMyProfileImageServiceResult;

public record GetMyProfileImageUseCaseResult(
		Long id,
		String imageUrl,
		boolean isRepresentative
) {
	public static GetMyProfileImageUseCaseResult from(GetMyProfileImageServiceResult response) {
		return new GetMyProfileImageUseCaseResult(
				response.id(),
				response.imageUrl(),
				response.isRepresentative()
		);
	}
}
