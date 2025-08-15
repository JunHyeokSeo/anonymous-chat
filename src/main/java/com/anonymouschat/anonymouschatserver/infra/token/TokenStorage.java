package com.anonymouschat.anonymouschatserver.infra.token;

import java.util.Optional;

public interface TokenStorage {
    void save(String key, String value, long expirationMillis);
    Optional<String> find(String key);
    void delete(String key);
}
