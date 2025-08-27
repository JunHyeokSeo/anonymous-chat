package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.application.dto.AuthServiceDto;
import com.anonymouschat.anonymouschatserver.application.dto.AuthUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.port.TokenStoragePort;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Role;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtTokenProvider;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

	@InjectMocks
	private AuthService authService;

	@Mock
	private TokenStoragePort tokenStorage;

	@Mock
	private JwtTokenProvider jwtProvider;

	@Mock
	private JwtValidator jwtValidator;

	@Nested
	@DisplayName("createAccessToken(OAuthProvider, providerId)")
	class CreateAccessTokenByOAuth {

		@Test
		@DisplayName("OAuth 정보 기반 액세스 토큰 정상 발급")
		void createAccessTokenWithOAuth() {
			when(jwtProvider.createAccessTokenForOAuthLogin(OAuthProvider.GOOGLE, "abc123"))
					.thenReturn("mock-token");

			String result = authService.createAccessToken(OAuthProvider.GOOGLE, "abc123");

			assertThat(result).isEqualTo("mock-token");
		}
	}

	@Nested
	@DisplayName("createAccessToken(userId, role)")
	class CreateAccessTokenByUserId {

		@Test
		@DisplayName("userId 기반 액세스 토큰 정상 발급")
		void createAccessTokenWithUserId() {
			when(jwtProvider.createAccessToken(1L, Role.USER)).thenReturn("access-token");

			String result = authService.createAccessToken(1L, Role.USER);

			assertThat(result).isEqualTo("access-token");
		}
	}

	@Nested
	@DisplayName("createRefreshToken()")
	class CreateRefreshToken {

		@Test
		@DisplayName("userId 기반 리프레시 토큰 정상 발급")
		void createRefreshToken() {
			when(jwtProvider.createRefreshToken(1L, Role.USER)).thenReturn("refresh-token");

			String result = authService.createRefreshToken(1L, Role.USER);

			assertThat(result).isEqualTo("refresh-token");
		}
	}

	@Nested
	@DisplayName("saveRefreshToken()")
	class SaveRefreshToken {

		@Test
		@DisplayName("리프레시 토큰 메타데이터와 함께 저장 성공")
		void saveTokenSuccessfully() {
			Long userId = 1L;
			String refreshToken = "refresh-token";
			String userAgent = "test-agent";
			String ipAddress = "127.0.0.1";

			authService.saveRefreshToken(userId, refreshToken, userAgent, ipAddress);

			verify(tokenStorage).storeRefreshToken(eq(userId), any(AuthServiceDto.RefreshTokenInfo.class));
		}
	}

	@Nested
	@DisplayName("getRefreshTokenInfo()")
	class GetRefreshTokenInfo {

		@Test
		@DisplayName("저장된 토큰 정보 정상 조회")
		void getExistingTokenInfo() {
			Long userId = 1L;
			AuthServiceDto.RefreshTokenInfo tokenInfo = AuthServiceDto.RefreshTokenInfo.builder()
					                                            .token("stored-token")
					                                            .userId(userId)
					                                            .issuedAt(LocalDateTime.now())
					                                            .expiresAt(LocalDateTime.now().plusDays(7))
					                                            .userAgent("test-agent")
					                                            .ipAddress("127.0.0.1")
					                                            .build();

			when(tokenStorage.getRefreshToken(userId)).thenReturn(Optional.of(tokenInfo));

			AuthServiceDto.RefreshTokenInfo result = authService.getRefreshTokenInfo(userId);

			assertThat(result).isEqualTo(tokenInfo);
		}

		@Test
		@DisplayName("토큰이 존재하지 않으면 예외 발생")
		void tokenNotFound() {
			Long userId = 1L;
			when(tokenStorage.getRefreshToken(userId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> authService.getRefreshTokenInfo(userId))
					.isInstanceOf(InvalidTokenException.class)
					.hasMessage(ErrorCode.INVALID_TOKEN.getMessage());
		}
	}

	@Nested
	@DisplayName("invalidateRefreshToken()")
	class InvalidateRefreshToken {

		@Test
		@DisplayName("토큰 삭제 동작 확인")
		void invalidateToken() {
			Long userId = 1L;

			authService.invalidateRefreshToken(userId);

			verify(tokenStorage).deleteRefreshToken(userId);
		}
	}

	@Nested
	@DisplayName("validateToken()")
	class ValidateToken {

		@Test
		@DisplayName("유효한 토큰 검증 통과")
		void validToken() {
			String token = "valid-token";
			String tokenHash = String.valueOf(token.hashCode());

			doNothing().when(jwtValidator).validate(token);
			when(tokenStorage.isTokenBlacklisted(tokenHash)).thenReturn(false);

			authService.validateToken(token);

			verify(jwtValidator).validate(token);
			verify(tokenStorage).isTokenBlacklisted(tokenHash);
		}

		@Test
		@DisplayName("블랙리스트된 토큰이면 예외 발생")
		void blacklistedToken() {
			String token = "blacklisted-token";
			String tokenHash = String.valueOf(token.hashCode());

			doNothing().when(jwtValidator).validate(token);
			when(tokenStorage.isTokenBlacklisted(tokenHash)).thenReturn(true);

			assertThatThrownBy(() -> authService.validateToken(token))
					.isInstanceOf(InvalidTokenException.class)
					.hasMessage(ErrorCode.INVALID_TOKEN.getMessage());
		}

		@Test
		@DisplayName("유효하지 않은 토큰이면 예외 발생")
		void invalidToken() {
			doThrow(new InvalidTokenException(ErrorCode.INVALID_TOKEN))
					.when(jwtValidator).validate("invalid");

			assertThatThrownBy(() -> authService.validateToken("invalid"))
					.isInstanceOf(InvalidTokenException.class);
		}
	}

	@Nested
	@DisplayName("extractUserId()")
	class ExtractUserId {

		@Test
		@DisplayName("토큰에서 userId 정상 추출")
		void extractUserId() {
			CustomPrincipal principal = CustomPrincipal.builder()
					                            .userId(99L)
					                            .role(Role.USER)
					                            .build();
			when(jwtProvider.getPrincipalFromToken("token")).thenReturn(principal);

			Long userId = authService.extractUserId("token");

			assertThat(userId).isEqualTo(99L);
		}
	}

	@Nested
	@DisplayName("blacklistAccessToken()")
	class BlacklistAccessToken {

		@Test
		@DisplayName("만료되지 않은 토큰 블랙리스트 추가")
		void blacklistNonExpiredToken() {
			String accessToken = "access-token";
			when(jwtProvider.getExpirationMillis(accessToken)).thenReturn(60000L); // 1분 남음

			authService.blacklistAccessToken(accessToken);

			verify(tokenStorage).blacklistToken(String.valueOf(accessToken.hashCode()), 60);
		}

		@Test
		@DisplayName("이미 만료된 토큰은 블랙리스트에 추가하지 않음")
		void skipExpiredToken() {
			String accessToken = "expired-token";
			when(jwtProvider.getExpirationMillis(accessToken)).thenReturn(0L); // 이미 만료

			authService.blacklistAccessToken(accessToken);

			verify(tokenStorage, never()).blacklistToken(anyString(), anyInt());
		}
	}

	@Nested
	@DisplayName("revokeAllUserTokens()")
	class RevokeAllUserTokens {

		@Test
		@DisplayName("사용자의 모든 토큰 무효화")
		void revokeAllTokens() {
			Long userId = 1L;

			authService.revokeAllUserTokens(userId);

			verify(tokenStorage).deleteAllUserTokens(userId);
		}
	}

	@Nested
	@DisplayName("storeOAuthTempData()")
	class StoreOAuthTempData {

		@Test
		@DisplayName("OAuth 임시 데이터 저장 성공")
		void storeOAuthTempDataSuccess() {
			AuthUseCaseDto.AuthTempData tempData = AuthUseCaseDto.AuthTempData.builder()
					                                        .accessToken("access-token")
					                                        .refreshToken("refresh-token")
					                                        .isGuestUser(false)
					                                        .userId(1L)
					                                        .userNickname("testUser")
					                                        .build();

			when(tokenStorage.storeOAuthTempData(any(AuthServiceDto.OAuthTempInfo.class)))
					.thenReturn("temp-code");

			String result = authService.storeOAuthTempData(tempData);

			assertThat(result).isEqualTo("temp-code");
			verify(tokenStorage).storeOAuthTempData(any(AuthServiceDto.OAuthTempInfo.class));
		}
	}

	@Nested
	@DisplayName("consumeOAuthTempData()")
	class ConsumeOAuthTempData {

		@Test
		@DisplayName("OAuth 임시 데이터 소비 성공")
		void consumeOAuthTempDataSuccess() {
			String tempCode = "temp-code";
			AuthServiceDto.OAuthTempInfo tempInfo = AuthServiceDto.OAuthTempInfo.builder()
					                                        .accessToken("access-token")
					                                        .refreshToken("refresh-token")
					                                        .isGuestUser(false)
					                                        .userId(1L)
					                                        .userNickname("testUser")
					                                        .build();

			when(tokenStorage.consumeOAuthTempData(tempCode))
					.thenReturn(Optional.of(tempInfo));

			AuthUseCaseDto.AuthTempData result = authService.consumeOAuthTempData(tempCode);

			assertThat(result.accessToken()).isEqualTo("access-token");
			assertThat(result.userId()).isEqualTo(1L);
			assertThat(result.userNickname()).isEqualTo("testUser");
		}

		@Test
		@DisplayName("존재하지 않는 임시 코드로 예외 발생")
		void consumeNonExistentTempData() {
			String tempCode = "invalid-code";
			when(tokenStorage.consumeOAuthTempData(tempCode))
					.thenReturn(Optional.empty());

			assertThatThrownBy(() -> authService.consumeOAuthTempData(tempCode))
					.isInstanceOf(InvalidTokenException.class)
					.hasMessage(ErrorCode.INVALID_TOKEN.getMessage());
		}
	}
}