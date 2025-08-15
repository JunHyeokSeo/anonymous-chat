package com.anonymouschat.anonymouschatserver.infra.security;

import com.anonymouschat.anonymouschatserver.application.dto.AuthTokens;
import com.anonymouschat.anonymouschatserver.application.usecase.AuthUseCase;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.UnauthorizedException;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final AuthUseCase authUseCase;
	private final OAuth2ProviderResolver oAuth2ProviderResolver;

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
	) throws IOException {

		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

		OAuthProvider provider = oAuth2ProviderResolver.resolve(oAuth2User);
		String providerId = provider.extractProviderId(oAuth2User.getAttributes());

		AuthTokens authTokens = authUseCase.login(provider, providerId);

		String json = String.format("""
		{
			"accessToken": "%s",
			"refreshToken": "%s"
		}
		""", authTokens.accessToken(), authTokens.refreshToken());

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
		response.setStatus(HttpServletResponse.SC_OK);
	}
}