package com.anonymouschat.anonymouschatserver.common.jwt;

import com.anonymouschat.anonymouschatserver.domain.user.type.OAuthProvider;

public record OAuthPrincipal(OAuthProvider provider, String providerId) {}

