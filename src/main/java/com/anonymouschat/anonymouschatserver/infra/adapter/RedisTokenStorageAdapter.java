package com.anonymouschat.anonymouschatserver.infra.adapter;

import com.anonymouschat.anonymouschatserver.application.dto.AuthServiceDto;
import com.anonymouschat.anonymouschatserver.application.port.TokenStoragePort;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.storage.TokenDeserializationException;
import com.anonymouschat.anonymouschatserver.common.exception.storage.TokenSerializationException;
import com.anonymouschat.anonymouschatserver.common.exception.storage.TokenStorageException;
import com.anonymouschat.anonymouschatserver.infra.adapter.dto.TokenStorageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisTokenStorageAdapter implements TokenStoragePort {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;
	private final SecureRandom secureRandom = new SecureRandom();

	private static final String OAUTH_TEMP_PREFIX = "oauth_temp:";
	private static final String REFRESH_PREFIX = "refresh:";
	private static final String BLACKLIST_PREFIX = "blacklist:";

	@Override
	public String storeOAuthTempData(AuthServiceDto.OAuthTempInfo data) {
		String tempCode = generateSecureCode();
		String key = OAUTH_TEMP_PREFIX + tempCode;
		TokenStorageDto.AuthTempData storageData = TokenStorageDto.AuthTempData.from(data);
		store(key, storageData, 300);
		return tempCode;
	}

	@Override
	public Optional<AuthServiceDto.OAuthTempInfo> consumeOAuthTempData(String tempCode) {
		String key = OAUTH_TEMP_PREFIX + tempCode;
		Optional<TokenStorageDto.AuthTempData> data = get(key, TokenStorageDto.AuthTempData.class);

		if (data.isPresent()) {
			try {
				redisTemplate.delete(key);
				log.debug("OAuth 임시 토큰 소비 - tempCode: {}", tempCode);
				return Optional.of(data.get().toServiceDto());
			} catch (Exception e) {
				log.error("OAuth 임시 토큰 삭제 실패 - key: {}", key, e);
				throw new TokenStorageException(ErrorCode.TOKEN_STORAGE_ERROR, e);
			}
		}
		return Optional.empty();
	}

	@Override
	public void storeRefreshToken(Long userId, AuthServiceDto.RefreshTokenInfo info) {
		String key = REFRESH_PREFIX + userId;
		TokenStorageDto.RefreshTokenData storageData = TokenStorageDto.RefreshTokenData.from(info);
		store(key, storageData, 604800);
		log.debug("Refresh 토큰 저장 - userId: {}", userId);
	}

	@Override
	public Optional<AuthServiceDto.RefreshTokenInfo> getRefreshToken(Long userId) {
		String key = REFRESH_PREFIX + userId;
		return get(key, TokenStorageDto.RefreshTokenData.class)
				       .map(TokenStorageDto.RefreshTokenData::toServiceDto);
	}

	@Override
	public void deleteRefreshToken(Long userId) {
		try {
			String key = REFRESH_PREFIX + userId;
			redisTemplate.delete(key);
			log.debug("Refresh 토큰 삭제 - userId: {}", userId);
		} catch (Exception e) {
			log.error("Refresh 토큰 삭제 실패 - userId: {}", userId, e);
			throw new TokenStorageException(ErrorCode.TOKEN_STORAGE_ERROR, e);
		}
	}

	@Override
	public void blacklistToken(String tokenHash, int ttlSeconds) {
		try {
			String key = BLACKLIST_PREFIX + tokenHash;
			redisTemplate.opsForValue().set(key, "blacklisted", Duration.ofSeconds(ttlSeconds));
			log.debug("Access 토큰 블랙리스트 추가 - hash: {}", tokenHash);
		} catch (Exception e) {
			log.error("토큰 블랙리스트 추가 실패 - hash: {}", tokenHash, e);
			throw new TokenStorageException(ErrorCode.TOKEN_STORAGE_ERROR, e);
		}
	}

	@Override
	public boolean isTokenBlacklisted(String tokenHash) {
		try {
			String key = BLACKLIST_PREFIX + tokenHash;
			return redisTemplate.hasKey(key);
		} catch (Exception e) {
			log.error("토큰 블랙리스트 확인 실패 - hash: {}", tokenHash, e);
			return true;
		}
	}

	@Override
	public void deleteAllUserTokens(Long userId) {
		try {
			Set<String> keysToDelete = scanKeys(REFRESH_PREFIX + userId + "*");
			if (!keysToDelete.isEmpty()) {
				redisTemplate.delete(keysToDelete);
				log.info("사용자 모든 토큰 무효화 - userId: {}, count: {}", userId, keysToDelete.size());
			}
		} catch (Exception e) {
			log.error("사용자 모든 토큰 삭제 실패 - userId: {}", userId, e);
			throw new TokenStorageException(ErrorCode.TOKEN_STORAGE_ERROR, e);
		}
	}

	@Override
	public boolean isAvailable() {
		try {
			Boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
				String pong = connection.ping();
				return "PONG".equalsIgnoreCase(pong);
			});
			return Boolean.TRUE.equals(result);
		} catch (Exception e) {
			log.error("Redis 연결 확인 실패", e);
			return false;
		}
	}

	private <T> void store(String key, T data, int ttlSeconds) {
		try {
			String json = objectMapper.writeValueAsString(data);
			redisTemplate.opsForValue().set(key, json, Duration.ofSeconds(ttlSeconds));
		} catch (JsonProcessingException e) {
			log.error("토큰 데이터 직렬화 실패 - key: {}", key, e);
			throw new TokenSerializationException(ErrorCode.TOKEN_SERIALIZATION_FAILED, e);
		} catch (Exception e) {
			log.error("토큰 저장 실패 - key: {}", key, e);
			throw new TokenStorageException(ErrorCode.TOKEN_STORAGE_ERROR, e);
		}
	}

	private <T> Optional<T> get(String key, Class<T> clazz) {
		try {
			String json = (String) redisTemplate.opsForValue().get(key);
			if (json == null) return Optional.empty();

			T data = objectMapper.readValue(json, clazz);
			return Optional.of(data);
		} catch (JsonProcessingException e) {
			log.error("토큰 데이터 역직렬화 실패 - key: {}", key, e);
			throw new TokenDeserializationException(ErrorCode.TOKEN_DESERIALIZATION_FAILED, e);
		} catch (Exception e) {
			log.error("토큰 조회 실패 - key: {}", key, e);
			throw new TokenStorageException(ErrorCode.TOKEN_STORAGE_ERROR, e);
		}
	}

	private String generateSecureCode() {
		try {
			byte[] bytes = new byte[32];
			secureRandom.nextBytes(bytes);
			return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		} catch (Exception e) {
			log.error("보안 코드 생성 실패", e);
			throw new TokenStorageException(ErrorCode.TOKEN_STORAGE_ERROR, e);
		}
	}

	/**
	 * Redis KEYS 대신 SCAN 기반 키 검색 (운영환경 안전성 고려)
	 */
	private Set<String> scanKeys(String pattern) {
		return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
			Set<String> keys = new HashSet<>();
			try (var cursor = connection.keyCommands()
					                  .scan(ScanOptions.scanOptions().match(pattern).count(1000).build())) {
				cursor.forEachRemaining(item -> keys.add(new String(item)));
			}
			return keys;
		});
	}
}