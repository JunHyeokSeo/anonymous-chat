package com.anonymouschat.anonymouschatserver.infra.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class HttpRequestExtractor {

	private static final String BEARER_PREFIX = "Bearer ";

	public String extractUserAgent(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		return userAgent != null ? userAgent : "Unknown";
	}

	public String extractClientIpAddress(HttpServletRequest request) {
		// X-Forwarded-For 헤더 확인 (프록시/로드밸런서 환경)
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
			return xForwardedFor.split(",")[0].trim();
		}

		// X-Real-IP 헤더 확인 (Nginx 등)
		String xRealIp = request.getHeader("X-Real-IP");
		if (xRealIp != null && !xRealIp.trim().isEmpty()) {
			return xRealIp.trim();
		}

		// X-Forwarded 헤더 확인
		String xForwarded = request.getHeader("X-Forwarded");
		if (xForwarded != null && !xForwarded.trim().isEmpty()) {
			return xForwarded.trim();
		}

		// 직접 연결된 클라이언트 IP
		String remoteAddr = request.getRemoteAddr();
		return remoteAddr != null ? remoteAddr : "Unknown";
	}

	public String extractAccessToken(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
			return authHeader.substring(BEARER_PREFIX.length());
		}
		return null;
	}
}