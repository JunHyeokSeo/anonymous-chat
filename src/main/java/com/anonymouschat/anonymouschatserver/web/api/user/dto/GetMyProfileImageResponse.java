package com.anonymouschat.anonymouschatserver.web.api.user.dto;

import com.anonymouschat.anonymouschatserver.application.dto.GetMyProfileImageResult;

public record GetMyProfileImageResponse(
		Long id,
		String imageUrl,
		boolean isRepresentative
) {
	public static GetMyProfileImageResponse from(GetMyProfileImageResult response) {
		return new GetMyProfileImageResponse(
				response.id(),
				response.imageUrl(),
				response.isRepresentative()
		);
	}
}
