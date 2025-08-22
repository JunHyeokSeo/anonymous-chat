package com.anonymouschat.anonymouschatserver.infra.security;

import com.anonymouschat.anonymouschatserver.application.dto.AuthUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.port.TokenStoragePort;
import com.anonymouschat.anonymouschatserver.application.usecase.AuthUseCase;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final AuthUseCase authUseCase;
	private final OAuth2ProviderResolver oAuth2ProviderResolver;
	private final TokenStoragePort tokenStoragePort;

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
	) throws IOException {

		try {
			OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
			OAuthProvider provider = oAuth2ProviderResolver.resolve(authentication);
			String providerId = provider.extractProviderId(oAuth2User.getAttributes());

			log.info("{}OAuth2 로그인 성공 - provider: {}, providerId: {}",
					LogTag.SECURITY_AUTHENTICATION, provider, providerId);

			AuthUseCaseDto.AuthResult authResult = authUseCase.login(provider, providerId);

			if (!tokenStoragePort.isAvailable()) {
				log.error("토큰 저장소 사용 불가 - fallback 처리");
				handleFallback(response, authResult);
				return;
			}

			String tempCode = authUseCase.storeOAuthTempData(authResult);
			response.sendRedirect("/auth/callback?code=" + tempCode);

		} catch (Exception e) {
			log.error("{}OAuth2 인증 처리 실패", LogTag.SECURITY_AUTHENTICATION, e);
			response.sendRedirect("/login?error=processing_failed");
		}
	}

	private void handleFallback(HttpServletResponse response, AuthUseCaseDto.AuthResult authResult)
			throws IOException {
		String redirectUrl = authResult.isGuestUser() ? "/register" : "/";
		redirectUrl += "#accessToken=" + authResult.accessToken();
		if (authResult.refreshToken() != null) {
			redirectUrl += "&refreshToken=" + authResult.refreshToken();
		}
		response.sendRedirect(redirectUrl);
	}
}