package com.anonymouschat.anonymouschatserver.common.security;

public record UserPrincipal(Long userId) implements OAuthPrincipal {
	@Override public PrincipalType getType() { return PrincipalType.ACCESS; }
	@Override public String getName() { return String.valueOf(userId); }
}
