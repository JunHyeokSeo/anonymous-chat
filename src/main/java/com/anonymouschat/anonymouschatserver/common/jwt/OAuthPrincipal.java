package com.anonymouschat.anonymouschatserver.common.jwt;

import com.anonymouschat.anonymouschatserver.domain.user.OAuthProvider;

public record OAuthPrincipal(OAuthProvider provider, String providerId) {}

