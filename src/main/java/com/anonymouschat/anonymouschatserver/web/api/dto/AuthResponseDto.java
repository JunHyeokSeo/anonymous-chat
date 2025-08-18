package com.anonymouschat.anonymouschatserver.web.api.dto;

import com.anonymouschat.anonymouschatserver.application.dto.AuthResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record AuthResponseDto(
		String accessToken,
		String refreshToken,
		Boolean isNewUser
) {
	public static AuthResponseDto from(AuthResult result) {
		return builder()
				       .accessToken(result.accessToken())
				       .refreshToken(result.refreshToken())
				       .isNewUser(result.isNewUser())
				       .build();
	}
}
