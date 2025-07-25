package com.anonymouschat.anonymouschatserver.common.security;

import com.anonymouschat.anonymouschatserver.domain.user.type.OAuthProvider;
import lombok.Getter;

@Getter
public class OAuthPrincipal implements java.security.Principal {

	private final OAuthProvider provider;
	private final String providerId;
	private final Long userId;

	public OAuthPrincipal(OAuthProvider provider, String providerId) {
		this.provider = provider;
		this.providerId = providerId;
		this.userId = null;
	}

	public OAuthPrincipal(OAuthProvider provider, String providerId, Long userId) {
		this.provider = provider;
		this.providerId = providerId;
		this.userId = userId;
	}

	@Override
	public String getName() {
		return provider.name() + ":" + providerId;
	}
}
