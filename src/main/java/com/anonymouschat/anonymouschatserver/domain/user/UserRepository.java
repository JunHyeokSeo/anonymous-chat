package com.anonymouschat.anonymouschatserver.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByProviderAndProviderId(OAuthProvider provider, String providerId);

	boolean existsByProviderAndProviderId(OAuthProvider provider, String providerId);
}

