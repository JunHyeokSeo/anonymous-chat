package com.anonymouschat.anonymouschatserver.common.security;

import com.anonymouschat.anonymouschatserver.common.jwt.JwtTokenProvider;
import com.anonymouschat.anonymouschatserver.domain.user.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.user.repository.UserRepository;
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
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
	) throws IOException {
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
		Object rawSub = oAuth2User.getAttribute("sub");
		if (rawSub == null) {
			throw new IllegalArgumentException("Google OAuth response does not contain 'sub'");
		}
		String providerId = rawSub.toString();

		boolean isNewUser = !userRepository.existsByProviderAndProviderId(OAuthProvider.GOOGLE, providerId);

		String accessToken = jwtTokenProvider.createAccessToken(OAuthProvider.GOOGLE, providerId);
		String refreshToken = jwtTokenProvider.createRefreshToken(OAuthProvider.GOOGLE, providerId);

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
		response.flushBuffer();
	}
}
