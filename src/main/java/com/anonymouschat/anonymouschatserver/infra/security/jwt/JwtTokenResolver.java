package com.anonymouschat.anonymouschatserver.infra.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰을 HTTP 또는 WebSocket 요청의 Authorization 헤더에서 추출하는 유틸리티 클래스입니다.
 * <p>
 * HTTP 기반 요청과 WebSocket 기반 요청 모두에서 사용할 수 있도록
 * 오버로드된 `resolve` 메서드를 제공합니다.
 */
@Component
public class JwtTokenResolver {

	private static final String BEARER_PREFIX = "Bearer ";

	/**
	 * HttpServletRequest 로부터 JWT 토큰을 추출합니다.
	 *
	 * @param request HttpServletRequest 객체
	 * @return Bearer 토큰 문자열 (접두사 제외), 존재하지 않으면 null 반환
	 */
	public String resolve(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		return extractBearerToken(authHeader);
	}

	/**
	 * ServerHttpRequest(WebSocket Handshake 요청 등)로부터 JWT 토큰을 추출합니다.
	 *
	 * @param request ServerHttpRequest 객체
	 * @return Bearer 토큰 문자열 (접두사 제외), 존재하지 않으면 null 반환
	 */
	public String resolve(ServerHttpRequest request) {
		HttpHeaders headers = request.getHeaders();
		String authHeader = headers.getFirst("Authorization");
		return extractBearerToken(authHeader);
	}

	/**
	 * Authorization 헤더 값에서 "Bearer " 접두사를 제거하고 토큰만 추출합니다.
	 *
	 * @param authHeader Authorization 헤더 문자열
	 * @return Bearer 토큰 문자열, 없으면 null
	 */
	private String extractBearerToken(String authHeader) {
		if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
			return authHeader.substring(BEARER_PREFIX.length());
		}
		return null;
	}
}
