package com.anonymouschat.anonymouschatserver.infra.token;

import java.util.Optional;

public interface TokenStorage {
    void save(Long key, String value, long expirationMillis);
    Optional<String> find(Long key);
    void delete(Long key);
}
