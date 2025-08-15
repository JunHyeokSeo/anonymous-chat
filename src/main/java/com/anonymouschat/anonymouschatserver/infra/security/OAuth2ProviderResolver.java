package com.anonymouschat.anonymouschatserver.infra.security;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.BadRequestException;
import com.anonymouschat.anonymouschatserver.common.exception.InternalServerException;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class OAuth2ProviderResolver {
	public OAuthProvider resolve(HttpServletRequest request, OAuth2User oAuth2User) {
		if (!(oAuth2User instanceof OAuth2AuthenticationToken token)) {
			throw new InternalServerException(ErrorCode.INVALID_OAUTH_TOKEN_TYPE);
		}

		String registrationId = token.getAuthorizedClientRegistrationId();

		return switch (registrationId.toLowerCase()) {
			case "google" -> OAuthProvider.GOOGLE;
			case "kakao" -> OAuthProvider.KAKAO;
			case "naver" -> OAuthProvider.NAVER;
			default -> throw new BadRequestException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
		};
	}
}