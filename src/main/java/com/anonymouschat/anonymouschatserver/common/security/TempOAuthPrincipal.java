package com.anonymouschat.anonymouschatserver.common.security;

import com.anonymouschat.anonymouschatserver.domain.user.type.OAuthProvider;

public record TempOAuthPrincipal(OAuthProvider provider, String providerId) implements OAuthPrincipal {
	@Override public PrincipalType getType() { return PrincipalType.REGISTRATION;}
	@Override public String getName() { return provider.name() + ":" + providerId; }
}
