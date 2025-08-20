package com.anonymouschat.anonymouschatserver.infra.security;

import com.anonymouschat.anonymouschatserver.application.dto.AuthUseCaseDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class TokenCookieManager {

	private static final String ACCESS_TOKEN = "accessToken";
	private static final String REFRESH_TOKEN = "refreshToken";

	/**
	 * User 토큰을 쿠키에 저장
	 */
	public void writeTokens(HttpServletResponse response, AuthUseCaseDto.AuthResult result) {
		addCookie(response, ACCESS_TOKEN, result.accessToken(), true);
		if (result.refreshToken() != null) {
			addCookie(response, REFRESH_TOKEN, result.refreshToken(), true);
		}
	}

	/**
	 * 기존 토큰(Guest 등)을 삭제
	 */
	public void clearTokens(HttpServletResponse response) {
		deleteCookie(response, ACCESS_TOKEN);
		deleteCookie(response, REFRESH_TOKEN);
	}

	private void addCookie(HttpServletResponse response, String name, String value, boolean httpOnly) {
		ResponseCookie cookie = ResponseCookie.from(name, value)
				                        .httpOnly(httpOnly)
//				                        .secure(true)
				                        .sameSite("Strict")
				                        .path("/")
				                        .build();
		response.addHeader("Set-Cookie", cookie.toString());
	}

	private void deleteCookie(HttpServletResponse response, String name) {
		ResponseCookie cookie = ResponseCookie.from(name, "")
				                        .httpOnly(true)
//				                        .secure(true)
				                        .sameSite("Strict")
				                        .path("/")
				                        .maxAge(0) // 즉시 만료
				                        .build();
		response.addHeader("Set-Cookie", cookie.toString());
	}
}
