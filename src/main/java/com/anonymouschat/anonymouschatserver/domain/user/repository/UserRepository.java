package com.anonymouschat.anonymouschatserver.domain.user.repository;

import com.anonymouschat.anonymouschatserver.domain.user.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByProviderAndProviderId(OAuthProvider provider, String providerId);

	boolean existsByProviderAndProviderId(OAuthProvider provider, String providerId);

	boolean existsByNickname(String nickname);
}

