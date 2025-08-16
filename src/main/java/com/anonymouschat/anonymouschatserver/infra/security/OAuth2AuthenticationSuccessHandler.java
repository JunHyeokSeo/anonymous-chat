package com.anonymouschat.anonymouschatserver.infra.security;

import com.anonymouschat.anonymouschatserver.application.dto.AuthResult;
import com.anonymouschat.anonymouschatserver.application.usecase.AuthUseCase;
import com.anonymouschat.anonymouschatserver.common.log.LogTag;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.AuthResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
	) throws IOException {

		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

		OAuthProvider provider = oAuth2ProviderResolver.resolve(oAuth2User);
		String providerId = provider.extractProviderId(oAuth2User.getAttributes());

		AuthResult authResult = authUseCase.login(provider, providerId);

		AuthResponseDto dto = AuthResponseDto.from(authResult);

		        log.info("{}provider={}, providerId={}", LogTag.SECURITY, provider, providerId);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);
		objectMapper.writeValue(response.getWriter(), dto);

		// TODO: 로그인 성공 후 사용자 역할이 GUEST인 경우 프로필 입력 페이지로, USER인 경우 홈 화면으로 리디렉션 처리
	}
}