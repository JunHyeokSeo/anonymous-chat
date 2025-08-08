package com.anonymouschat.anonymouschatserver.infra.security;

import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.Objects;

/**
 * @param userId     Nullable
 * @param provider   Nullable
 * @param providerId Nullable
 * @param role       NotNull
 */
@Builder
public record CustomPrincipal(
		Long userId,
		OAuthProvider provider,
		String providerId,
		String role) implements Principal {
	public CustomPrincipal {
		//userId가 없으면 provider + providerId는 있어야 한다
		if (userId == null) {
			if (provider == null || providerId == null || providerId.isBlank()) {
				throw new IllegalArgumentException("userId 또는 provider + providerId 조합 중 하나는 반드시 존재해야 합니다.");
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
		throw new IllegalStateException("CustomPrincipal은 userId 또는 provider + providerId 조합 중 하나를 반드시 포함해야 합니다.");
	}

	public boolean isAuthenticatedUser() {
		return userId != null;
	}
}
