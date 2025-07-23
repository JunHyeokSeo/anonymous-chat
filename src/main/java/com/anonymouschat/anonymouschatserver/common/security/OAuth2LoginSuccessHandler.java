package com.anonymouschat.anonymouschatserver.common.security;

import com.anonymouschat.anonymouschatserver.common.jwt.JwtTokenProvider;
import com.anonymouschat.anonymouschatserver.domain.user.entity.User;
import com.anonymouschat.anonymouschatserver.domain.user.repository.UserRepository;
import com.anonymouschat.anonymouschatserver.domain.user.type.OAuthProvider;
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
		OAuthProvider provider = OAuthProvider.GOOGLE;

		// 기존 회원 여부 확인
		User user = userRepository.findByProviderAndProviderId(provider, providerId).orElse(null);

		String json;
		if (user == null) {
			// 신규 회원 → 임시 토큰 발급
			String temporaryToken = jwtTokenProvider.createTemporaryToken(provider, providerId);

			json = String.format("""
			{
				"accessToken": "%s",
				"isNewUser": true
			}
			""", temporaryToken);

		} else {
			// 기존 회원 → 실제 토큰 및 리프레시 토큰 발급
			String accessToken = jwtTokenProvider.createUserToken(user.getId());
			String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

			json = String.format("""
			{
				"accessToken": "%s",
				"refreshToken": "%s",
				"isNewUser": false
			}
			""", accessToken, refreshToken);
		}

		// 응답 전송
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
