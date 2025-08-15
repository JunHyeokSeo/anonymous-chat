package com.anonymouschat.anonymouschatserver.application.dto;

public record AuthResult(AuthTokens tokens, boolean isNewUser) {}
