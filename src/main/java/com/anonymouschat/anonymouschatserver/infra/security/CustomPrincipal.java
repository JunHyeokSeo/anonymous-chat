package com.anonymouschat.anonymouschatserver.infra.security;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.InternalServerException;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import jakarta.annotation.Nullable;
import lombok.Builder;

import java.security.Principal;

/**
 * @param userId     Nullable
 * @param provider   Nullable
 * @param providerId Nullable
 * @param role       NotNull
 */
@Builder
public record CustomPrincipal(
		@Nullable Long userId,
		@Nullable OAuthProvider provider,
		@Nullable String providerId,
		String role) implements Principal {
	public CustomPrincipal {
		//userId가 없으면 provider + providerId는 있어야 한다
		if (userId == null) {
			if (provider == null || providerId == null || providerId.isBlank()) {
				throw new InternalServerException(ErrorCode.INVALID_PRINCIPAL_STATE);
			}
		}
	}

	@Override
	public String getName() {
		if (userId != null) {
			return "user:" + userId;
		}
		if (provider != null && providerId != null) {
			return provider.name() + ":" + providerId;
		}
		throw new InternalServerException(ErrorCode.INVALID_PRINCIPAL_STATE);
	}

	public boolean isAuthenticatedUser() {
		return userId != null;
	}
}