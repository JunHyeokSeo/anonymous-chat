package com.anonymouschat.anonymouschatserver.infra.security.jwt;

import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰을 HTTP 또는 WebSocket 요청에서 추출하는 유틸리티 클래스입니다.
 * 우선순위:
 *   1) Authorization 헤더 (Bearer)
 *   2) accessToken 쿠키
 */
@Component
@Slf4j
public class JwtTokenResolver {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String ACCESS_TOKEN_COOKIE = "accessToken";

	public String resolve(HttpServletRequest request) {
		// Authorization 헤더 먼저 확인
		String authHeader = request.getHeader("Authorization");
		String token = extractBearerToken(authHeader);

		// 헤더 없으면 쿠키 확인
		if (token == null && request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
					token = cookie.getValue();
					log.debug("{}HTTP 요청에서 JWT 추출 완료 (쿠키) - token={}", LogTag.SECURITY_JWT, token);
					break;
				}
			}
		}

		if (token != null) {
			log.debug("{}HTTP 요청에서 JWT 추출 완료 - token={}", LogTag.SECURITY_JWT, token);
		} else {
			log.trace("{}HTTP 요청에서 JWT 없음 - URI={}", LogTag.SECURITY_JWT, request.getRequestURI());
		}
		return token;
	}

	public String resolve(ServerHttpRequest request) {
		// WebSocket 요청은 헤더만 확인
		String authHeader = request.getHeaders().getFirst("Authorization");
		String token = extractBearerToken(authHeader);

		if (token != null) {
			log.debug("{}WebSocket 요청에서 JWT 추출 완료 - token={}", LogTag.SECURITY_JWT, token);
		} else {
			log.trace("{}WebSocket 요청에서 JWT 없음 - URI={}", LogTag.SECURITY_JWT, request.getURI());
		}
		return token;
	}

	private String extractBearerToken(String authHeader) {
		if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
			return authHeader.substring(BEARER_PREFIX.length());
		}
		return null;
	}
}
