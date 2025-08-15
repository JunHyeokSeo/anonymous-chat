package com.anonymouschat.anonymouschatserver.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthTokens(String accessToken, @Nullable String refreshToken) {
}
