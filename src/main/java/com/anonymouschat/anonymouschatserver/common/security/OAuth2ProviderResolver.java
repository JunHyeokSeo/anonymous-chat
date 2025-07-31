package com.anonymouschat.anonymouschatserver.common.security;

import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class OAuth2ProviderResolver {
	public OAuthProvider resolve(HttpServletRequest request, OAuth2User oAuth2User) {
		if (!(oAuth2User instanceof OAuth2AuthenticationToken token)) {
			throw new IllegalArgumentException("OAuth2AuthenticationToken이 아닙니다.");
		}

		String registrationId = token.getAuthorizedClientRegistrationId();

		return switch (registrationId.toLowerCase()) {
			case "google" -> OAuthProvider.GOOGLE;
			case "kakao" -> OAuthProvider.KAKAO;
			case "naver" -> OAuthProvider.NAVER;
			default -> throw new IllegalArgumentException("지원하지 않는 OAuth Provider: " + registrationId);
		};
	}
}
