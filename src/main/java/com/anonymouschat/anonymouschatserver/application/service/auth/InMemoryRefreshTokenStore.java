package com.anonymouschat.anonymouschatserver.application.service.auth;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

	private final Map<Long, String> store = new ConcurrentHashMap<>();

	@Override
	public void save(Long userId, String refreshToken) {
		store.put(userId, refreshToken);
	}

	@Override
	public String get(Long userId) {
		return store.get(userId);
	}

	@Override
	public void remove(Long userId) {
		store.remove(userId);
	}

	@Override
	public boolean matches(Long userId, String refreshToken) {
		return refreshToken.equals(store.get(userId));
	}
}
