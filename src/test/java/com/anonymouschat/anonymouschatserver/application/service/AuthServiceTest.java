package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtTokenProvider;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtValidator;
import com.anonymouschat.anonymouschatserver.infra.token.TokenStorage;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {
	@InjectMocks
	private AuthService authService;

	@Mock
	private TokenStorage tokenStorage;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private JwtValidator jwtValidator;

	@Nested
	@DisplayName("generateAccessToken(OAuthProvider, providerId)")
	class GenerateAccessTokenByOAuth {

		@Test
		@DisplayName("OAuth 정보 기반 액세스 토큰 정상 발급")
		void generateAccessTokenWithOAuth() {
			when(jwtTokenProvider.createAccessTokenForOAuthLogin(OAuthProvider.GOOGLE, "abc123"))
					.thenReturn("mock-token");

			String result = authService.generateAccessToken(OAuthProvider.GOOGLE, "abc123");

			assertThat(result).isEqualTo("mock-token");
		}
	}

	@Nested
	@DisplayName("generateAccessToken(userId, role)")
	class GenerateAccessTokenByUserId {

		@Test
		@DisplayName("userId 기반 액세스 토큰 정상 발급")
		void generateAccessTokenWithUserId() {
			when(jwtTokenProvider.createAccessToken(1L, "ROLE_USER")).thenReturn("access-token");

			String result = authService.generateAccessToken(1L, "ROLE_USER");

			assertThat(result).isEqualTo("access-token");
		}
	}

	@Nested
	@DisplayName("generateRefreshToken")
	class GenerateRefreshToken {

		@Test
		@DisplayName("userId 기반 리프레시 토큰 정상 발급")
		void generateRefreshToken() {
			when(jwtTokenProvider.createRefreshToken(1L, "ROLE_USER")).thenReturn("refresh-token");

			String result = authService.generateRefreshToken(1L, "ROLE_USER");

			assertThat(result).isEqualTo("refresh-token");
		}
	}

	@Nested
	@DisplayName("saveRefreshToken")
	class SaveRefreshToken {

		@Test
		@DisplayName("토큰 유효시간 기반 저장 성공")
		void saveTokenSuccessfully() {
			when(jwtTokenProvider.getExpirationMillis("refresh-token")).thenReturn(3000L);

			authService.saveRefreshToken("1", "refresh-token");

			verify(tokenStorage).save("1", "refresh-token", 3000L);
		}
	}

	@Nested
	@DisplayName("findRefreshToken")
	class FindRefreshToken {

		@Test
		@DisplayName("저장된 토큰 정상 조회")
		void findExistingToken() {
			when(tokenStorage.find("1")).thenReturn(Optional.of("stored-token"));

			Optional<String> result = authService.findRefreshTokenOrThrow("1").describeConstable();

			assertThat(result).contains("stored-token");
		}

		@Test
		@DisplayName("토큰이 존재하지 않으면 empty 반환")
		void tokenNotFound() {
			doThrow(new InvalidTokenException(ErrorCode.INVALID_TOKEN)).when(tokenStorage).find("1");

			assertThatThrownBy(() -> authService.findRefreshTokenOrThrow("1").describeConstable())
					.isInstanceOf(InvalidTokenException.class);
		}
	}

	@Nested
	@DisplayName("revokeRefreshToken")
	class RevokeRefreshToken {

		@Test
		@DisplayName("토큰 삭제 동작 확인")
		void revokeToken() {
			authService.revokeRefreshToken("1");

			verify(tokenStorage).delete("1");
		}
	}

	@Nested
	@DisplayName("validateToken")
	class ValidateToken {

		@Test
		@DisplayName("유효한 토큰 검증 통과")
		void validToken() {
			doNothing().when(jwtValidator).validate("token");

			authService.validateToken("token");

			verify(jwtValidator).validate("token");
		}

		@Test
		@DisplayName("유효하지 않은 토큰이면 예외 발생")
		void invalidToken() {
			doThrow(new InvalidTokenException(ErrorCode.INVALID_TOKEN)).when(jwtValidator).validate("invalid");

			assertThatThrownBy(() -> authService.validateToken("invalid"))
					.isInstanceOf(InvalidTokenException.class);
		}
	}

	@Nested
	@DisplayName("getUserIdFromToken")
	class GetUserIdFromToken {

		@Test
		@DisplayName("토큰에서 userId 정상 추출")
		void extractUserId() {
			CustomPrincipal principal = CustomPrincipal.builder().userId(99L).role("ROLE_USER").build();
			when(jwtTokenProvider.getPrincipalFromToken("token")).thenReturn(principal);

			Long userId = authService.getUserIdFromToken("token");

			assertThat(userId).isEqualTo(99L);
		}
	}
}
