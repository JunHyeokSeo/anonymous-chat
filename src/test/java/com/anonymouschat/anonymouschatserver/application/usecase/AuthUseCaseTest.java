package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.AuthResult;
import com.anonymouschat.anonymouschatserver.application.dto.AuthTokens;
import com.anonymouschat.anonymouschatserver.application.service.AuthService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.testsupport.util.TestUtils;
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
@DisplayName("AuthUseCase 테스트")
class AuthUseCaseTest {

	@InjectMocks
	AuthUseCase authUseCase;

	@Mock
	AuthService authService;

	@Mock
	UserService userService;


	@Nested
	@DisplayName("login()")
	class Login {

		@Test
		@DisplayName("기존 유저 로그인 시 refreshToken 포함 반환")
		void loginExistingUser() throws Exception {
			User existingUser = TestUtils.createUser(1L);

			when(userService.findByProviderAndProviderId(OAuthProvider.KAKAO, "abc123"))
					.thenReturn(Optional.of(existingUser));
			when(authService.generateAccessToken(OAuthProvider.KAKAO, "abc123"))
					.thenReturn("access-token");
			when(authService.generateRefreshToken(1L, "ROLE_USER"))
					.thenReturn("refresh-token");

			AuthResult result = authUseCase.login(OAuthProvider.KAKAO, "abc123");

			assertThat(result.tokens().accessToken()).isEqualTo("access-token");
			assertThat(result.tokens().refreshToken()).isEqualTo("refresh-token");
			assertThat(result.isNewUser()).isFalse();

			verify(authService).saveRefreshToken("1", "refresh-token");
		}

		@Test
		@DisplayName("신규 유저 로그인 시 guest로 생성되고 refreshToken 없이 반환")
		void loginNewUserAsGuest() {
			User guestUser = TestUtils.guestUser();

			when(userService.findByProviderAndProviderId(OAuthProvider.KAKAO, "new-user"))
					.thenReturn(Optional.empty());
			when(userService.createGuestUser(OAuthProvider.KAKAO, "new-user"))
					.thenReturn(guestUser);
			when(authService.generateAccessToken(OAuthProvider.KAKAO, "new-user"))
					.thenReturn("access-token");

			AuthResult result = authUseCase.login(OAuthProvider.KAKAO, "new-user");

			assertThat(result.tokens().accessToken()).isEqualTo("access-token");
			assertThat(result.tokens().refreshToken()).isNull();
			assertThat(result.isNewUser()).isTrue();

			verify(authService, never()).generateRefreshToken(any(), any());
		}
	}

	@Nested
	@DisplayName("refresh()")
	class Refresh {

		@Test
		@DisplayName("유효한 리프레시 토큰으로 재발급 성공")
		void validRefreshToken() throws Exception {
			String oldToken = "old-token";
			Long userId = 99L;
			User user = TestUtils.createUser(userId);

			when(authService.getUserIdFromToken(oldToken)).thenReturn(userId);
			when(authService.findRefreshTokenOrThrow(String.valueOf(userId))).thenReturn(oldToken);
			when(userService.findUser(userId)).thenReturn(user);
			when(authService.generateAccessToken(userId, "ROLE_USER")).thenReturn("new-access");
			when(authService.generateRefreshToken(userId, "ROLE_USER")).thenReturn("new-refresh");

			AuthTokens tokens = authUseCase.refresh(oldToken);

			assertThat(tokens.accessToken()).isEqualTo("new-access");
			assertThat(tokens.refreshToken()).isEqualTo("new-refresh");

			verify(authService).revokeRefreshToken(String.valueOf(userId));
			verify(authService).saveRefreshToken(String.valueOf(userId), "new-refresh");
		}

		@Test
		@DisplayName("저장된 토큰과 다르면 탈취로 간주 후 예외 발생")
		void tokenTheftDetected() {
			String oldToken = "fake-token";
			Long userId = 50L;

			when(authService.getUserIdFromToken(oldToken)).thenReturn(userId);
			when(authService.findRefreshTokenOrThrow(String.valueOf(userId))).thenReturn("stored-different-token");

			assertThatThrownBy(() -> authUseCase.refresh(oldToken))
					.isInstanceOf(InvalidTokenException.class)
					.hasMessage(ErrorCode.TOKEN_THEFT_DETECTED.getMessage());

			verify(authService).revokeRefreshToken(String.valueOf(userId));
		}

		@Test
		@DisplayName("토큰 유효성 검사 실패 시 예외 발생")
		void refreshTokenInvalid() {
			doThrow(new InvalidTokenException(ErrorCode.EXPIRED_TOKEN))
					.when(authService).validateToken("expired");

			assertThatThrownBy(() -> authUseCase.refresh("expired"))
					.isInstanceOf(InvalidTokenException.class)
					.hasMessage(ErrorCode.EXPIRED_TOKEN.getMessage());
		}
	}

	@Nested
	@DisplayName("logout()")
	class Logout {
		@Test
		@DisplayName("refresh 토큰 정상 삭제")
		void logout() {
			authUseCase.logout("1");

			verify(authService).revokeRefreshToken("1");
		}
	}
}
