
package com.anonymouschat.anonymouschatserver.application.service.auth;

import com.anonymouschat.anonymouschatserver.common.jwt.JwtTokenProvider;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.RefreshTokenResponse;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

	@Mock
	JwtTokenProvider jwtTokenProvider;

	@Mock
	RefreshTokenStore refreshTokenStore;

	@InjectMocks
	AuthService authService;

	private final Long userId = 1L;
	private final String oldRefreshToken = "old-refresh-token";
	private final String newAccessToken = "new-access-token";
	private final String newRefreshToken = "new-refresh-token";

	@Nested
	@DisplayName("reissueAccessToken 메서드는")
	class ReissueAccessToken {

		@Test
		@DisplayName("정상적인 토큰이면 새로운 토큰을 발급한다")
		void reissueAccessToken_success() {
			when(jwtTokenProvider.validateToken(oldRefreshToken)).thenReturn(true);
			when(jwtTokenProvider.getUserIdFromToken(oldRefreshToken)).thenReturn(userId);
			when(refreshTokenStore.matches(userId, oldRefreshToken)).thenReturn(true);
			when(jwtTokenProvider.createAccessToken(userId)).thenReturn(newAccessToken);
			when(jwtTokenProvider.createRefreshToken(userId)).thenReturn(newRefreshToken);

			RefreshTokenResponse response = authService.reissueAccessToken(oldRefreshToken);

			assertThat(response.accessToken()).isEqualTo(newAccessToken);
			assertThat(response.refreshToken()).isEqualTo(newRefreshToken);
			verify(refreshTokenStore).save(userId, newRefreshToken);
		}

		@Test
		@DisplayName("유효하지 않은 토큰이면 JwtException이 발생한다")
		void reissueAccessToken_invalidToken() {
			when(jwtTokenProvider.validateToken(oldRefreshToken)).thenReturn(false);

			assertThatThrownBy(() -> authService.reissueAccessToken(oldRefreshToken))
					.isInstanceOf(JwtException.class)
					.hasMessage("유효하지 않은 Refresh Token 입니다.");
		}

		@Test
		@DisplayName("refreshToken이 저장된 값과 다르면 IllegalStateException이 발생한다")
		void reissueAccessToken_mismatchedToken() {
			when(jwtTokenProvider.validateToken(oldRefreshToken)).thenReturn(true);
			when(jwtTokenProvider.getUserIdFromToken(oldRefreshToken)).thenReturn(userId);
			when(refreshTokenStore.matches(userId, oldRefreshToken)).thenReturn(false);

			assertThatThrownBy(() -> authService.reissueAccessToken(oldRefreshToken))
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("저장된 토큰과 일치하지 않습니다.");
		}
	}

	@Nested
	@DisplayName("logout 메서드는")
	class Logout {

		@Test
		@DisplayName("refreshToken을 삭제한다")
		void logout_success() {
			authService.logout(userId);
			verify(refreshTokenStore).remove(userId);
		}
	}

	@Nested
	@DisplayName("saveRefreshToken 메서드는")
	class SaveRefreshToken {

		@Test
		@DisplayName("refreshToken을 저장한다")
		void saveRefreshToken_success() {
			authService.saveRefreshToken(userId, oldRefreshToken);
			verify(refreshTokenStore).save(userId, oldRefreshToken);
		}
	}
}
