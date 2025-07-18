package com.anonymouschat.anonymouschatserver.web.api.auth.dto;

public record RefreshTokenResponse(String accessToken, String refreshToken) { }