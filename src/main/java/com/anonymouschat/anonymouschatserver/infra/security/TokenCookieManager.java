package com.anonymouschat.anonymouschatserver.infra.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TokenCookieManager {
	private static final String REFRESH_TOKEN = "refreshToken";

	/**
	 * Refresh 토큰만 쿠키에 저장
	 */
	public void writeRefreshToken(HttpServletResponse response, String refreshToken) {
		if (refreshToken != null) {
			log.debug("REFRESH 토큰 쿠키 설정 완료 - {}", refreshToken);
			ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN, refreshToken)
					                        .httpOnly(true)
					                        .secure(true)   // 배포 환경에서는 true
					                        .sameSite("Strict")
					                        .path("/")
					                        .build();
			response.addHeader("Set-Cookie", cookie.toString());
		}
	}

	/**
	 * Refresh 토큰 쿠키 삭제
	 */
	public void clearRefreshToken(HttpServletResponse response) {
		ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN, "")
				                        .httpOnly(true)
				                        .secure(true)
				                        .sameSite("Strict")
				                        .path("/")
				                        .maxAge(0)
				                        .build();
		response.addHeader("Set-Cookie", cookie.toString());
		log.debug("REFRESH 토큰 쿠키에서 삭제 완료");
	}
}
