package com.anonymouschat.anonymouschatserver.common.security;

import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.Objects;

/**
 * @param userId     Nullable
 * @param provider   NotNull
 * @param providerId NotNull
 */
@Builder
public record CustomPrincipal(Long userId, OAuthProvider provider, String providerId) implements Principal {
	@Override
	public String getName() {
		return provider.name() + ":" + providerId;
	}

	public boolean isAuthenticatedUser() {
		return userId != null;
	}
}
