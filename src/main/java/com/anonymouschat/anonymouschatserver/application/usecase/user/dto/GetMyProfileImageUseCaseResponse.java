package com.anonymouschat.anonymouschatserver.application.usecase.user.dto;

import com.anonymouschat.anonymouschatserver.application.service.dto.GetMyProfileImageServiceResponse;

public record GetMyProfileImageUseCaseResponse(
		Long id,
		String imageUrl,
		boolean isRepresentative
) {
	public static GetMyProfileImageUseCaseResponse from(GetMyProfileImageServiceResponse response) {
		return new GetMyProfileImageUseCaseResponse(
				response.id(),
				response.imageUrl(),
				response.isRepresentative()
		);
	}
}
