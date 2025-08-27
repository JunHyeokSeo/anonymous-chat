package com.anonymouschat.anonymouschatserver.application.dto;

import lombok.Builder;
import java.time.LocalDateTime;

public class AuthServiceDto {

	@Builder
	public record RefreshTokenInfo(
			String token,
			Long userId,
			LocalDateTime issuedAt,
			LocalDateTime expiresAt,
			String userAgent,
			String ipAddress
	) {
		public static RefreshTokenInfo create(Long userId, String token, String userAgent, String ipAddress) {
			return RefreshTokenInfo.builder()
					       .token(token)
					       .userId(userId)
					       .issuedAt(LocalDateTime.now())
					       .expiresAt(LocalDateTime.now().plusSeconds(604800))
					       .userAgent(userAgent)
					       .ipAddress(ipAddress)
					       .build();
		}
	}

	@Builder
	public record OAuthTempInfo(
			String accessToken,
			String refreshToken,
			boolean isGuestUser,
			Long userId,
			String userNickname
	) {
		public static OAuthTempInfo from(AuthUseCaseDto.AuthTempData data) {
			return OAuthTempInfo.builder()
					       .accessToken(data.accessToken())
					       .refreshToken(data.refreshToken())
					       .isGuestUser(data.isGuestUser())
					       .userId(data.userId())
					       .userNickname(data.userNickname())
					       .build();
		}

		public AuthUseCaseDto.AuthTempData toUseCaseDto() {
			return AuthUseCaseDto.AuthTempData.builder()
					       .accessToken(accessToken)
					       .refreshToken(refreshToken)
					       .isGuestUser(isGuestUser)
					       .userId(userId)
					       .userNickname(userNickname)
					       .build();
		}
	}
}