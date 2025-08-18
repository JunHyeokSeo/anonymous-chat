package com.anonymouschat.anonymouschatserver.domain.type;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
	GUEST,
	USER,
	ADMIN;

	@Override
	public String getAuthority() {
		return "ROLE_" + this.name();
	}
}
