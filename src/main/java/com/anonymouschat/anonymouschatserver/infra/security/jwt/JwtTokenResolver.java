package com.anonymouschat.anonymouschatserver.infra.security.jwt;

import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰을 HTTP 또는 WebSocket 요청의 Authorization 헤더에서 추출하는 유틸리티 클래스입니다.
 * <p>
 * HTTP 기반 요청과 WebSocket 기반 요청 모두에서 사용할 수 있도록
 * 오버로드된 `resolve` 메서드를 제공합니다.
 */
@Component
@Slf4j
public class JwtTokenResolver {

	private static final String BEARER_PREFIX = "Bearer ";

	public String resolve(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		String token = extractBearerToken(authHeader);
		if (token != null) {
			log.debug("{}HTTP 요청에서 JWT 추출 완료 - token={}", LogTag.SECURITY_JWT, token);
		} else {
			log.trace("{}HTTP 요청에서 JWT 없음 - URI={}", LogTag.SECURITY_JWT, request.getRequestURI());
		}
		return token;
	}

	public String resolve(ServerHttpRequest request) {
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
