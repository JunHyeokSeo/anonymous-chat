package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.AuthServiceDto;
import com.anonymouschat.anonymouschatserver.application.dto.AuthUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.service.AuthService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Role;
import com.anonymouschat.testsupport.util.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
			when(authService.createAccessToken(OAuthProvider.KAKAO, "abc123"))
					.thenReturn("access-token");
			when(authService.createRefreshToken(1L, Role.USER))
					.thenReturn("refresh-token");

			AuthUseCaseDto.AuthData result = authUseCase.login(OAuthProvider.KAKAO, "abc123");

			assertThat(result.accessToken()).isEqualTo("access-token");
			assertThat(result.refreshToken()).isEqualTo("refresh-token");
			assertThat(result.isGuestUser()).isFalse();
			assertThat(result.userId()).isEqualTo(1L);
			assertThat(result.userNickname()).isEqualTo(existingUser.getNickname());
		}

		@Test
		@DisplayName("신규 유저 로그인 시 guest로 생성되고 refreshToken 없이 반환")
		void loginNewUserAsGuest() {
			User guestUser = TestUtils.guestUser();

			when(userService.findByProviderAndProviderId(OAuthProvider.KAKAO, "new-user"))
					.thenReturn(Optional.empty());
			when(userService.createGuestUser(OAuthProvider.KAKAO, "new-user"))
					.thenReturn(guestUser);
			when(authService.createAccessToken(OAuthProvider.KAKAO, "new-user"))
					.thenReturn("access-token");

			AuthUseCaseDto.AuthData result = authUseCase.login(OAuthProvider.KAKAO, "new-user");

			assertThat(result.accessToken()).isEqualTo("access-token");
			assertThat(result.refreshToken()).isNull();
			assertThat(result.isGuestUser()).isTrue();
			assertThat(result.userId()).isEqualTo(guestUser.getId());
			assertThat(result.userNickname()).isEqualTo(guestUser.getNickname());

			verify(authService, never()).createRefreshToken(any(), any());
		}
	}

	@Nested
	@DisplayName("refreshByUserId()")
	class RefreshByUserId {

		@Test
		@DisplayName("유효한 사용자 ID로 토큰 재발급 성공")
		void validUserIdRefresh() throws Exception {
			Long userId = 99L;
			String userAgent = "test-agent";
			String ipAddress = "127.0.0.1";
			User user = TestUtils.createUser(userId);

			AuthServiceDto.RefreshTokenInfo tokenInfo = AuthServiceDto.RefreshTokenInfo.builder()
					                                            .token("stored-refresh-token")
					                                            .userId(userId)
					                                            .issuedAt(LocalDateTime.now())
					                                            .expiresAt(LocalDateTime.now().plusDays(7))
					                                            .userAgent("old-agent")
					                                            .ipAddress("old-ip")
					                                            .build();

			when(authService.getRefreshTokenInfo(userId)).thenReturn(tokenInfo);
			when(userService.findUser(userId)).thenReturn(user);
			when(authService.createAccessToken(userId, Role.USER)).thenReturn("new-access");
			when(authService.createRefreshToken(userId, Role.USER)).thenReturn("new-refresh");

			AuthUseCaseDto.AuthTokens tokens = authUseCase.refreshByUserId(userId, userAgent, ipAddress);

			assertThat(tokens.accessToken()).isEqualTo("new-access");

			verify(authService).validateToken("stored-refresh-token");
			verify(authService).invalidateRefreshToken(userId);
			verify(authService).saveRefreshToken(userId, "new-refresh", userAgent, ipAddress);
		}

		@Test
		@DisplayName("저장된 RefreshToken이 없으면 예외 발생")
		void noStoredRefreshToken() {
			Long userId = 50L;
			String userAgent = "test-agent";
			String ipAddress = "127.0.0.1";

			when(authService.getRefreshTokenInfo(userId))
					.thenThrow(new InvalidTokenException(ErrorCode.INVALID_TOKEN));

			assertThatThrownBy(() -> authUseCase.refreshByUserId(userId, userAgent, ipAddress))
					.isInstanceOf(InvalidTokenException.class)
					.hasMessage(ErrorCode.INVALID_TOKEN.getMessage());

			verify(authService, never()).invalidateRefreshToken(userId);
			verify(authService, never()).saveRefreshToken(any(), any(), any(), any());
		}

		@Test
		@DisplayName("저장된 RefreshToken 검증 실패 시 예외 발생")
		void storedRefreshTokenValidationFailed() {
			Long userId = 99L;
			String userAgent = "test-agent";
			String ipAddress = "127.0.0.1";

			AuthServiceDto.RefreshTokenInfo tokenInfo = AuthServiceDto.RefreshTokenInfo.builder()
					                                            .token("invalid-refresh-token")
					                                            .userId(userId)
					                                            .issuedAt(LocalDateTime.now())
					                                            .expiresAt(LocalDateTime.now().plusDays(7))
					                                            .userAgent("test-agent")
					                                            .ipAddress("127.0.0.1")
					                                            .build();

			when(authService.getRefreshTokenInfo(userId)).thenReturn(tokenInfo);
			doThrow(new InvalidTokenException(ErrorCode.EXPIRED_TOKEN))
					.when(authService).validateToken("invalid-refresh-token");

			assertThatThrownBy(() -> authUseCase.refreshByUserId(userId, userAgent, ipAddress))
					.isInstanceOf(InvalidTokenException.class)
					.hasMessage(ErrorCode.EXPIRED_TOKEN.getMessage());

			verify(authService, never()).invalidateRefreshToken(userId);
			verify(authService, never()).saveRefreshToken(any(), any(), any(), any());
		}

		@Test
		@DisplayName("사용자 조회 실패 시 예외 발생")
		void userNotFound() {
			Long userId = 99L;
			String userAgent = "test-agent";
			String ipAddress = "127.0.0.1";

			AuthServiceDto.RefreshTokenInfo tokenInfo = AuthServiceDto.RefreshTokenInfo.builder()
					                                            .token("valid-refresh-token")
					                                            .userId(userId)
					                                            .issuedAt(LocalDateTime.now())
					                                            .expiresAt(LocalDateTime.now().plusDays(7))
					                                            .userAgent("test-agent")
					                                            .ipAddress("127.0.0.1")
					                                            .build();

			when(authService.getRefreshTokenInfo(userId)).thenReturn(tokenInfo);
			when(userService.findUser(userId))
					.thenThrow(new RuntimeException("User not found"));

			assertThatThrownBy(() -> authUseCase.refreshByUserId(userId, userAgent, ipAddress))
					.isInstanceOf(RuntimeException.class)
					.hasMessage("User not found");

			verify(authService).validateToken("valid-refresh-token");
			verify(authService).invalidateRefreshToken(userId);
			verify(authService, never()).saveRefreshToken(any(), any(), any(), any());
		}
	}

	@Nested
	@DisplayName("logout()")
	class Logout {

		@Test
		@DisplayName("refresh 토큰 및 access 토큰 무효화 처리")
		void logout() {
			Long userId = 1L;
			String accessToken = "access-token";

			authUseCase.logout(userId, accessToken);

			verify(authService).invalidateRefreshToken(userId);
			verify(authService).blacklistAccessToken(accessToken);
		}
	}

	@Nested
	@DisplayName("storeOAuthTempData()")
	class StoreOAuthTempData {

		@Test
		@DisplayName("OAuth 임시 데이터 저장 성공")
		void storeOAuthTempDataSuccess() {
			AuthUseCaseDto.AuthData authResult = AuthUseCaseDto.AuthData.builder()
					                                       .accessToken("access-token")
					                                       .refreshToken("refresh-token")
					                                       .isGuestUser(false)
					                                       .userId(1L)
					                                       .userNickname("testUser")
					                                       .build();

			when(authService.storeOAuthTempData(any(AuthUseCaseDto.AuthTempData.class)))
					.thenReturn("temp-code");

			String result = authUseCase.storeOAuthTempData(authResult);

			assertThat(result).isEqualTo("temp-code");
			verify(authService).storeOAuthTempData(any(AuthUseCaseDto.AuthTempData.class));
		}
	}

	@Nested
	@DisplayName("consumeOAuthTempData()")
	class ConsumeOAuthTempData {

		@Test
		@DisplayName("OAuth 임시 데이터 소비 성공")
		void consumeOAuthTempDataSuccess() {
			String tempCode = "temp-code";
			AuthUseCaseDto.AuthTempData expectedData = AuthUseCaseDto.AuthTempData.builder()
					                                            .accessToken("access-token")
					                                            .refreshToken("refresh-token")
					                                            .isGuestUser(false)
					                                            .userId(1L)
					                                            .userNickname("testUser")
					                                            .build();

			when(authService.consumeOAuthTempData(tempCode)).thenReturn(expectedData);

			AuthUseCaseDto.AuthTempData result = authUseCase.consumeOAuthTempData(tempCode);

			assertThat(result).isEqualTo(expectedData);
			verify(authService).consumeOAuthTempData(tempCode);
		}
	}

	@Nested
	@DisplayName("saveRefreshToken()")
	class SaveRefreshToken {

		@Test
		@DisplayName("리프레시 토큰 저장 성공")
		void saveRefreshTokenSuccess() {
			Long userId = 1L;
			String refreshToken = "refresh-token";
			String userAgent = "test-agent";
			String ipAddress = "127.0.0.1";

			authUseCase.saveRefreshToken(userId, refreshToken, userAgent, ipAddress);

			verify(authService).saveRefreshToken(userId, refreshToken, userAgent, ipAddress);
		}
	}

	@Nested
	@DisplayName("getUserIdFromToken()")
	class GetUserIdFromToken {

		@Test
		@DisplayName("토큰에서 userId 추출 성공")
		void getUserIdFromTokenSuccess() {
			String token = "test-token";
			Long expectedUserId = 1L;

			when(authService.extractUserId(token)).thenReturn(expectedUserId);

			Long result = authUseCase.getUserIdFromToken(token);

			assertThat(result).isEqualTo(expectedUserId);
			verify(authService).extractUserId(token);
		}
	}
}