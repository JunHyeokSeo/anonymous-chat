package com.anonymouschat.anonymouschatserver.application.port;

import com.anonymouschat.anonymouschatserver.application.dto.AuthServiceDto;
import java.util.Optional;

public interface TokenStoragePort {

	String storeOAuthTempData(AuthServiceDto.OAuthTempInfo data);
	Optional<AuthServiceDto.OAuthTempInfo> consumeOAuthTempData(String code);

	void storeRefreshToken(Long userId, AuthServiceDto.RefreshTokenInfo info);
	Optional<AuthServiceDto.RefreshTokenInfo> getRefreshToken(Long userId);
	void deleteRefreshToken(Long userId);

	void blacklistToken(String tokenHash, int ttlSeconds);
	boolean isTokenBlacklisted(String tokenHash);

	void deleteAllUserTokens(Long userId);
	boolean isAvailable();
}