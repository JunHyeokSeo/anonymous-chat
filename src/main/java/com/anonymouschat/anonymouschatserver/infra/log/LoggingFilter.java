package com.anonymouschat.anonymouschatserver.infra.log;

import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Role;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class LoggingFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

	@Value("${spring.profiles.active:dev}")
	private String activeProfile;

	private static final Map<String, String> MASKING_FIELDS = Map.of(
			"token", "***"
	);

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String uri = request.getRequestURI();

		// PageController 경로
		if (uri.equals("/")
				    || uri.startsWith("/login")
				    || uri.startsWith("/register")
				    || uri.startsWith("/chat")
				    || uri.startsWith("/chat-list")
				    || uri.startsWith("/profile")
				    || uri.startsWith("/user")
				    || uri.startsWith("/auth/callback")) {
			return true;
		}

		// 정적 리소스
		return uri.endsWith(".html")
				       || uri.startsWith("/css")
				       || uri.startsWith("/js")
				       || uri.startsWith("/images")
				       || uri.startsWith("/favicon")
				       || uri.startsWith("/webjars")
				       || uri.startsWith("/static");
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
	                                @NonNull HttpServletResponse response,
	                                FilterChain filterChain)
			throws ServletException, IOException {

		ContentCachingRequestWrapper cachingRequest = new ContentCachingRequestWrapper(request);
		ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);

		try {
			filterChain.doFilter(cachingRequest, cachingResponse);
		} finally {
			logRequest(cachingRequest);
			logResponse(cachingResponse);
			cachingResponse.copyBodyToResponse();
		}
	}

	private void logRequest(ContentCachingRequestWrapper request) {
		String method = request.getMethod();
		String uri = request.getRequestURI();
		String queryString = request.getQueryString();
		String principalInfo = getPrincipalInfo();

		if (isProd()) {
			log.info("REQUEST: user=[{}], method=[{}], uri=[{}], query=[{}]",
					principalInfo, method, uri, queryString);
		} else {
			String body = maskSensitive(getStringValue(request.getContentAsByteArray()));
			log.info("REQUEST: user=[{}], method=[{}], uri=[{}], query=[{}], body={}",
					principalInfo, method, uri, queryString, body);
		}
	}

	private void logResponse(ContentCachingResponseWrapper response) {
		int status = response.getStatus();
		String contentType = response.getContentType();

		if (isProd()) {
			log.info("RESPONSE: status=[{}]", status);
		} else {
			if (contentType != null && contentType.contains("application/json")) {
				String body = maskSensitive(getStringValue(response.getContentAsByteArray()));
				log.info("RESPONSE: status=[{}], body={}", status, body);
			} else {
				log.info("RESPONSE: status=[{}], body=[non-JSON omitted] ", status);
			}
		}
	}


	private String getPrincipalInfo() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return "anonymous";
		}
		Object principal = authentication.getPrincipal();
		if (principal instanceof CustomPrincipal(
				Long userId, OAuthProvider provider, String providerId, Role role
		)) {
			return "id=" + userId + ", role=" + role + ", provider=" + provider + ", providerId=" + providerId;
		}
		return principal.toString();
	}

	private String getStringValue(byte[] content) {
		if (content == null || content.length == 0) {
			return "";
		}
		return new String(content, StandardCharsets.UTF_8);
	}

	private String maskSensitive(String body) {
		if (body == null || body.isBlank()) return body;
		String masked = body;
		for (String key : MASKING_FIELDS.keySet()) {
			masked = masked.replaceAll(
					"(\"" + key + "\"\\s*:\\s*\").*?(\")",
					"$1" + MASKING_FIELDS.get(key) + "$2"
			);
		}
		return masked;
	}

	private boolean isProd() {
		return "prod".equalsIgnoreCase(activeProfile);
	}
}
