package com.anonymouschat.anonymouschatserver.application.service.auth;

public interface RefreshTokenStore {
	void save(Long userId, String refreshToken);
	String get(Long userId);
	void remove(Long userId);
	boolean matches(Long userId, String refreshToken);
}