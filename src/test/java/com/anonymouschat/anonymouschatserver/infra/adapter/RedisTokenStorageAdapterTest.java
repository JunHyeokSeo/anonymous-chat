package com.anonymouschat.anonymouschatserver.infra.adapter;

import com.anonymouschat.anonymouschatserver.application.dto.AuthServiceDto;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.storage.TokenDeserializationException;
import com.anonymouschat.anonymouschatserver.common.exception.storage.TokenSerializationException;
import com.anonymouschat.anonymouschatserver.common.exception.storage.TokenStorageException;
import com.anonymouschat.anonymouschatserver.infra.adapter.dto.TokenStorageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisTokenStorageAdapter 테스트")
class RedisTokenStorageAdapterTest {

	@InjectMocks
	private RedisTokenStorageAdapter redisTokenStorageAdapter;

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ObjectMapper objectMapper;

	@Spy
	private SecureRandom secureRandom;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@Nested
	@DisplayName("storeOAuthTempData()")
	class StoreOAuthTempData {

		@Test
		@DisplayName("OAuth 임시 데이터 저장 성공")
		void storeOAuthTempDataSuccess() throws JsonProcessingException {
			// given
			AuthServiceDto.OAuthTempInfo tempInfo = AuthServiceDto.OAuthTempInfo.builder()
					                                        .accessToken("access-token")
					                                        .refreshToken("refresh-token")
					                                        .isGuestUser(false)
					                                        .userId(1L)
					                                        .userNickname("testUser")
					                                        .build();

			TokenStorageDto.AuthTempData storageData = TokenStorageDto.AuthTempData.from(tempInfo);
			String expectedJson = "{\"accessToken\":\"access-token\"}";

			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(objectMapper.writeValueAsString(storageData)).thenReturn(expectedJson);

			// when
			String result = redisTokenStorageAdapter.storeOAuthTempData(tempInfo);

			// then
			assertThat(result).isNotNull();
			verify(objectMapper).writeValueAsString(storageData);
			verify(valueOperations).set(eq("oauth_temp:" + result), eq(expectedJson), eq(Duration.ofSeconds(300)));
		}

		@Test
		@DisplayName("직렬화 실패 시 TokenSerializationException 발생")
		void storeOAuthTempDataSerializationFailed() throws JsonProcessingException {
			// given
			AuthServiceDto.OAuthTempInfo tempInfo = AuthServiceDto.OAuthTempInfo.builder()
					                                        .accessToken("access-token")
					                                        .build();

			when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("serialization error") {});

			// when & then
			assertThatThrownBy(() -> redisTokenStorageAdapter.storeOAuthTempData(tempInfo))
					.isInstanceOf(TokenSerializationException.class)
					.hasMessage(ErrorCode.TOKEN_SERIALIZATION_FAILED.getMessage());
		}
	}

	@Nested
	@DisplayName("consumeOAuthTempData()")
	class ConsumeOAuthTempData {

		@Test
		@DisplayName("OAuth 임시 데이터 소비 성공")
		void consumeOAuthTempDataSuccess() throws JsonProcessingException {
			// given
			String tempCode = "test-code";
			String key = "oauth_temp:" + tempCode;
			String jsonData = "{\"accessToken\":\"access-token\"}";

			TokenStorageDto.AuthTempData storageData = TokenStorageDto.AuthTempData.builder()
					                                            .accessToken("access-token")
					                                            .refreshToken("refresh-token")
					                                            .isGuestUser(false)
					                                            .userId(1L)
					                                            .userNickname("testUser")
					                                            .build();

			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(valueOperations.get(key)).thenReturn(jsonData);
			when(objectMapper.readValue(jsonData, TokenStorageDto.AuthTempData.class)).thenReturn(storageData);

			// when
			Optional<AuthServiceDto.OAuthTempInfo> result = redisTokenStorageAdapter.consumeOAuthTempData(tempCode);

			// then
			assertThat(result).isPresent();
			assertThat(result.get().accessToken()).isEqualTo("access-token");
			verify(redisTemplate).delete(key);
		}

		@Test
		@DisplayName("존재하지 않는 임시 코드로 빈 Optional 반환")
		void consumeOAuthTempDataNotFound() {
			// given
			String tempCode = "non-existent-code";
			String key = "oauth_temp:" + tempCode;

			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(valueOperations.get(key)).thenReturn(null);

			// when
			Optional<AuthServiceDto.OAuthTempInfo> result = redisTokenStorageAdapter.consumeOAuthTempData(tempCode);

			// then
			assertThat(result).isEmpty();
			verify(redisTemplate, never()).delete(key);
		}

		@Test
		@DisplayName("역직렬화 실패 시 TokenDeserializationException 발생")
		void consumeOAuthTempDataDeserializationFailed() throws JsonProcessingException {
			// given
			String tempCode = "test-code";
			String key = "oauth_temp:" + tempCode;
			String invalidJson = "invalid-json";

			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(valueOperations.get(key)).thenReturn(invalidJson);
			when(objectMapper.readValue(invalidJson, TokenStorageDto.AuthTempData.class))
					.thenThrow(new JsonProcessingException("deserialization error") {});

			// when & then
			assertThatThrownBy(() -> redisTokenStorageAdapter.consumeOAuthTempData(tempCode))
					.isInstanceOf(TokenDeserializationException.class)
					.hasMessage(ErrorCode.TOKEN_DESERIALIZATION_FAILED.getMessage());
		}
	}

	@Nested
	@DisplayName("storeRefreshToken()")
	class StoreRefreshToken {

		@Test
		@DisplayName("Refresh 토큰 저장 성공")
		void storeRefreshTokenSuccess() throws JsonProcessingException {
			// given
			Long userId = 1L;
			AuthServiceDto.RefreshTokenInfo tokenInfo = AuthServiceDto.RefreshTokenInfo.builder()
					                                            .token("refresh-token")
					                                            .userId(userId)
					                                            .issuedAt(LocalDateTime.now())
					                                            .expiresAt(LocalDateTime.now().plusDays(7))
					                                            .userAgent("test-agent")
					                                            .ipAddress("127.0.0.1")
					                                            .build();

			String expectedJson = "{\"token\":\"refresh-token\"}";
			String key = "refresh:" + userId;

			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(objectMapper.writeValueAsString(any(TokenStorageDto.RefreshTokenData.class))).thenReturn(expectedJson);

			// when
			redisTokenStorageAdapter.storeRefreshToken(userId, tokenInfo);

			// then
			verify(objectMapper).writeValueAsString(any(TokenStorageDto.RefreshTokenData.class));
			verify(valueOperations).set(key, expectedJson, Duration.ofSeconds(604800));
		}
	}

	@Nested
	@DisplayName("getRefreshToken()")
	class GetRefreshToken {

		@Test
		@DisplayName("Refresh 토큰 조회 성공")
		void getRefreshTokenSuccess() throws JsonProcessingException {
			// given
			Long userId = 1L;
			String key = "refresh:" + userId;
			String jsonData = "{\"token\":\"refresh-token\"}";

			TokenStorageDto.RefreshTokenData storageData = TokenStorageDto.RefreshTokenData.builder()
					                                               .token("refresh-token")
					                                               .userId(userId)
					                                               .issuedAt(LocalDateTime.now().toString())
					                                               .expiresAt(LocalDateTime.now().plusDays(7).toString())
					                                               .userAgent("test-agent")
					                                               .ipAddress("127.0.0.1")
					                                               .build();

			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(valueOperations.get(key)).thenReturn(jsonData);
			when(objectMapper.readValue(jsonData, TokenStorageDto.RefreshTokenData.class)).thenReturn(storageData);

			// when
			Optional<AuthServiceDto.RefreshTokenInfo> result = redisTokenStorageAdapter.getRefreshToken(userId);

			// then
			assertThat(result).isPresent();
			assertThat(result.get().token()).isEqualTo("refresh-token");
			assertThat(result.get().userId()).isEqualTo(userId);
		}

		@Test
		@DisplayName("존재하지 않는 토큰으로 빈 Optional 반환")
		void getRefreshTokenNotFound() {
			// given
			Long userId = 1L;
			String key = "refresh:" + userId;

			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(valueOperations.get(key)).thenReturn(null);

			// when
			Optional<AuthServiceDto.RefreshTokenInfo> result = redisTokenStorageAdapter.getRefreshToken(userId);

			// then
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("deleteRefreshToken()")
	class DeleteRefreshToken {

		@Test
		@DisplayName("Refresh 토큰 삭제 성공")
		void deleteRefreshTokenSuccess() {
			// given
			Long userId = 1L;
			String key = "refresh:" + userId;

			// when
			redisTokenStorageAdapter.deleteRefreshToken(userId);

			// then
			verify(redisTemplate).delete(key);
		}

		@Test
		@DisplayName("Redis 삭제 실패 시 TokenStorageException 발생")
		void deleteRefreshTokenFailed() {
			// given
			Long userId = 1L;
			String key = "refresh:" + userId;

			when(redisTemplate.delete(key)).thenThrow(new RuntimeException("Redis error"));

			// when & then
			assertThatThrownBy(() -> redisTokenStorageAdapter.deleteRefreshToken(userId))
					.isInstanceOf(TokenStorageException.class)
					.hasMessage(ErrorCode.TOKEN_STORAGE_ERROR.getMessage());
		}
	}

	@Nested
	@DisplayName("blacklistToken()")
	class BlacklistToken {

		@Test
		@DisplayName("토큰 블랙리스트 추가 성공")
		void blacklistTokenSuccess() {
			// given
			String tokenHash = "token-hash";
			int ttlSeconds = 3600;
			String key = "blacklist:" + tokenHash;

			when(redisTemplate.opsForValue()).thenReturn(valueOperations);

			// when
			redisTokenStorageAdapter.blacklistToken(tokenHash, ttlSeconds);

			// then
			verify(valueOperations).set(key, "blacklisted", Duration.ofSeconds(ttlSeconds));
		}

		@Test
		@DisplayName("Redis 오류 시 TokenStorageException 발생")
		void blacklistTokenFailed() {
			// given
			String tokenHash = "token-hash";
			int ttlSeconds = 3600;

			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			doThrow(new RuntimeException("Redis error"))
					.when(valueOperations).set(anyString(), anyString(), any(Duration.class));

			// when & then
			assertThatThrownBy(() -> redisTokenStorageAdapter.blacklistToken(tokenHash, ttlSeconds))
					.isInstanceOf(TokenStorageException.class)
					.hasMessage(ErrorCode.TOKEN_STORAGE_ERROR.getMessage());
		}
	}

	@Nested
	@DisplayName("isTokenBlacklisted()")
	class IsTokenBlacklisted {

		@Test
		@DisplayName("블랙리스트된 토큰 확인 성공")
		void isTokenBlacklistedTrue() {
			// given
			String tokenHash = "token-hash";
			String key = "blacklist:" + tokenHash;

			when(redisTemplate.hasKey(key)).thenReturn(true);

			// when
			boolean result = redisTokenStorageAdapter.isTokenBlacklisted(tokenHash);

			// then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("블랙리스트되지 않은 토큰 확인")
		void isTokenBlacklistedFalse() {
			// given
			String tokenHash = "token-hash";
			String key = "blacklist:" + tokenHash;

			when(redisTemplate.hasKey(key)).thenReturn(false);

			// when
			boolean result = redisTokenStorageAdapter.isTokenBlacklisted(tokenHash);

			// then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Redis 오류 시 안전하게 true 반환")
		void isTokenBlacklistedError() {
			// given
			String tokenHash = "token-hash";
			String key = "blacklist:" + tokenHash;

			when(redisTemplate.hasKey(key)).thenThrow(new RuntimeException("Redis error"));

			// when
			boolean result = redisTokenStorageAdapter.isTokenBlacklisted(tokenHash);

			// then
			assertThat(result).isTrue(); // 보안상 안전한 방향으로 true 반환
		}
	}

	@Nested
	@DisplayName("deleteAllUserTokens()")
	class DeleteAllUserTokens {

		@Test
		@DisplayName("사용자 모든 토큰 삭제 성공")
		void deleteAllUserTokensSuccess() {
			// given
			Long userId = 1L;
			Set<String> keys = Set.of("refresh:1:device1", "refresh:1:device2");

			when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Object>>any())).thenReturn(keys);

			// when
			redisTokenStorageAdapter.deleteAllUserTokens(userId);

			// then
			verify(redisTemplate).execute(ArgumentMatchers.<RedisCallback<Object>>any());
			verify(redisTemplate).delete(keys);
		}

		@Test
		@DisplayName("삭제할 토큰이 없는 경우")
		void deleteAllUserTokensEmpty() {
			// given
			Long userId = 1L;
			Set<String> emptyKeys = Set.of();

			when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Object>>any())).thenReturn(emptyKeys);

			// when
			redisTokenStorageAdapter.deleteAllUserTokens(userId);

			// then
			verify(redisTemplate).execute(ArgumentMatchers.<RedisCallback<Object>>any());
			verify(redisTemplate, never()).delete(anySet());
		}

		@Test
		@DisplayName("Redis 오류 시 TokenStorageException 발생")
		void deleteAllUserTokensError() {
			// given
			Long userId = 1L;

			when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Object>>any())).thenThrow(new RuntimeException("Redis error"));

			// when & then
			assertThatThrownBy(() -> redisTokenStorageAdapter.deleteAllUserTokens(userId))
					.isInstanceOf(TokenStorageException.class)
					.hasMessage(ErrorCode.TOKEN_STORAGE_ERROR.getMessage());
		}
	}

	@Nested
	@DisplayName("isAvailable()")
	class IsAvailable {

		@Test
		@DisplayName("Redis 연결 상태 확인 성공")
		void isAvailableTrue() {
			// given
			when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Object>>any())).thenReturn(true);

			// when
			boolean result = redisTokenStorageAdapter.isAvailable();

			// then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Redis 연결 실패")
		void isAvailableFalse() {
			// given
			when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Object>>any())).thenReturn(false);

			// when
			boolean result = redisTokenStorageAdapter.isAvailable();

			// then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("Redis 연결 예외 발생")
		void isAvailableException() {
			// given
			when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Object>>any())).thenThrow(new RuntimeException("Connection error"));

			// when
			boolean result = redisTokenStorageAdapter.isAvailable();

			// then
			assertThat(result).isFalse();
		}
	}
}