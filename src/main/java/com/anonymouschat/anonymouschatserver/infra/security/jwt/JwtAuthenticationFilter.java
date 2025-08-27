package com.anonymouschat.anonymouschatserver.infra.security.jwt;

import com.anonymouschat.anonymouschatserver.application.service.AuthService;
import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenResolver tokenResolver;
	private final JwtAuthenticationFactory authFactory;
	private final AuthService authService;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
	                                @NonNull HttpServletResponse response,
	                                @NonNull FilterChain filterChain) throws IOException, ServletException {

		try {
			String token = tokenResolver.resolve(request);

			if (token == null) {
				log.trace("{}JWT 토큰 없음 - URI: {}", LogTag.SECURITY_FILTER, request.getRequestURI());
			} else {
				processToken(token, request);
			}
		} catch (Exception e) {
			log.error("{}JWT 필터 처리 실패 - URI: {}", LogTag.SECURITY_FILTER, request.getRequestURI(), e);
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}

	private void processToken(String token, HttpServletRequest request) {
		try {
			// JWT 유효성 검증 (블랙리스트 확인 포함)
			authService.validateToken(token);

			CustomPrincipal principal = authFactory.createPrincipal(token);
			var authentication = authFactory.createAuthentication(principal);

			if (authentication != null) {
				SecurityContext context = SecurityContextHolder.createEmptyContext();
				context.setAuthentication(authentication);
				SecurityContextHolder.setContext(context);

				request.setAttribute("AuthenticatedPrincipal", principal);
				log.debug("{}인증 성공 - userId: {}", LogTag.SECURITY_AUTHENTICATION, principal.userId());
			}

		} catch (ExpiredJwtException e) {
			log.warn("{}만료된 토큰", LogTag.SECURITY_JWT);
			SecurityContextHolder.clearContext();
		} catch (AuthenticationException e) {
			log.warn("{}유효하지 않은 JWT: {}", LogTag.SECURITY_JWT, e.getMessage());
			SecurityContextHolder.clearContext();
		}
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return "OPTIONS".equalsIgnoreCase(request.getMethod());
	}
}