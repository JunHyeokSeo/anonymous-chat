package com.anonymouschat.anonymouschatserver.common.security;

public interface OAuthPrincipal{
	PrincipalType getType();
	String getName();
}
