package com.anonymouschat.anonymouschatserver.infra.token;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class InMemoryTokenStorage implements TokenStorage {

    private final Map<Long, TokenEntry> storage = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public InMemoryTokenStorage() {
        scheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public void save(Long key, String value, long expirationMillis) {
        long expiryTime = System.currentTimeMillis() + expirationMillis;
        storage.put(key, new TokenEntry(value, expiryTime));
    }

    @Override
    public Optional<String> find(Long key) {
        TokenEntry entry = storage.get(key);
        if (entry != null && !entry.isExpired()) {
            return Optional.of(entry.value());
        }

        if (entry != null && entry.isExpired()) {
            storage.remove(key);
        }
        return Optional.empty();
    }

    @Override
    public void delete(Long key) {
        storage.remove(key);
    }

    private void cleanupExpiredTokens() {
        storage.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private record TokenEntry(String value, long expiryTime) {
        boolean isExpired() {
            return System.currentTimeMillis() >= expiryTime;
        }
    }
}
