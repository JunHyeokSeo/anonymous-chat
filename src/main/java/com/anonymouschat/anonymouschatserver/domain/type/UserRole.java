package com.anonymouschat.anonymouschatserver.domain.type;

public enum UserRole {
	ROLE_GUEST,
	ROLE_USER,
	ROLE_ADMIN;

	public String getAuthority() {
		return this.name();
	}
}
