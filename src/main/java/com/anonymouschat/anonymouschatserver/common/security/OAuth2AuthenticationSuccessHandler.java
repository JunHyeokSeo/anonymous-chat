package com.anonymouschat.anonymouschatserver.common.security;

import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.jwt.JwtTokenProvider;
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

	private final JwtTokenProvider jwtTokenProvider;
	private final UserService userService;
	private final OAuth2ProviderResolver oAuth2ProviderResolver;

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
	) throws IOException {

		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
		Object rawSub = oAuth2User.getAttribute("sub");
		if (rawSub == null) {
			throw new IllegalArgumentException("Google OAuth 응답에 'sub' 값이 포함되어 있지 않습니다.");
		}
		String providerId = rawSub.toString();
		OAuthProvider provider = oAuth2ProviderResolver.resolve(request, oAuth2User);

		boolean isNewUser = userService.findByProviderAndProviderId(provider, providerId).isEmpty();

		String accessToken = jwtTokenProvider.createAccessToken(provider, providerId);
		String refreshToken = jwtTokenProvider.createRefreshToken(provider, providerId);

		String json = String.format("""
		{
			"accessToken": "%s",
			"refreshToken": "%s",
			"isNewUser": %s
		}
		""", accessToken, refreshToken, isNewUser);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
