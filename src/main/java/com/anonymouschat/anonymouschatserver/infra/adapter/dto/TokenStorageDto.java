package com.anonymouschat.anonymouschatserver.infra.adapter.dto;

import com.anonymouschat.anonymouschatserver.application.dto.AuthServiceDto;
import lombok.Builder;

public class TokenStorageDto {

	@Builder
	public record RefreshTokenData(
			String token,
			Long userId,
			String issuedAt,
			String expiresAt,
			String userAgent,
			String ipAddress
	) {
		public static RefreshTokenData from(AuthServiceDto.RefreshTokenInfo info) {
			return RefreshTokenData.builder()
					       .token(info.token())
					       .userId(info.userId())
					       .issuedAt(info.issuedAt().toString())
					       .expiresAt(info.expiresAt().toString())
					       .userAgent(info.userAgent())
					       .ipAddress(info.ipAddress())
					       .build();
		}

		public AuthServiceDto.RefreshTokenInfo toServiceDto() {
			return AuthServiceDto.RefreshTokenInfo.builder()
					       .token(token)
					       .userId(userId)
					       .issuedAt(java.time.LocalDateTime.parse(issuedAt))
					       .expiresAt(java.time.LocalDateTime.parse(expiresAt))
					       .userAgent(userAgent)
					       .ipAddress(ipAddress)
					       .build();
		}
	}

	@Builder
	public record OAuthTempData(
			String accessToken,
			String refreshToken,
			boolean isGuestUser,
			Long userId,
			String userNickname
	) {
		public static OAuthTempData from(AuthServiceDto.OAuthTempInfo info) {
			return OAuthTempData.builder()
					       .accessToken(info.accessToken())
					       .refreshToken(info.refreshToken())
					       .isGuestUser(info.isGuestUser())
					       .userId(info.userId())
					       .userNickname(info.userNickname())
					       .build();
		}

		public AuthServiceDto.OAuthTempInfo toServiceDto() {
			return AuthServiceDto.OAuthTempInfo.builder()
					       .accessToken(accessToken)
					       .refreshToken(refreshToken)
					       .isGuestUser(isGuestUser)
					       .userId(userId)
					       .userNickname(userNickname)
					       .build();
		}
	}
}