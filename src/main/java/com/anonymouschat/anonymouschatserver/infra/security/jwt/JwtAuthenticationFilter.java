package com.anonymouschat.anonymouschatserver.infra.security.jwt;

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

	private static final String ATTR_PRINCIPAL = "AuthenticatedPrincipal";

	private final JwtTokenResolver tokenResolver;
	private final JwtValidator jwtValidator;
	private final JwtAuthenticationFactory authFactory;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
	                                @NonNull HttpServletResponse response,
	                                @NonNull FilterChain filterChain) throws IOException, ServletException {

		try {
			String token = tokenResolver.resolve(request);

			if (token == null) {
				log.trace("{}요청에 JWT 토큰 없음 - URI={}", LogTag.SECURITY_FILTER, request.getRequestURI());
				// 토큰이 없으면 비인증 상태로 진행
			} else {
				log.debug("{}JWT 토큰 감지됨 - token={}", LogTag.SECURITY_JWT, token);

				try {
					jwtValidator.validate(token);  // 여기서 만료/유효성 실패 발생 가능
					log.debug("{}JWT 유효성 검사 통과", LogTag.SECURITY_JWT);

					CustomPrincipal principal = authFactory.createPrincipal(token);
					log.debug("{}Principal 생성 완료 - userId={}, role={}",
							LogTag.SECURITY_AUTHENTICATION, principal.userId(), principal.role());

					request.setAttribute(ATTR_PRINCIPAL, principal);

					SecurityContextHolder.clearContext();
					var authentication = authFactory.createAuthentication(principal);
					if (authentication != null) {
						SecurityContext context = SecurityContextHolder.createEmptyContext();
						context.setAuthentication(authentication);
						SecurityContextHolder.setContext(context);
						log.debug("{}SecurityContext에 Authentication 설정 완료", LogTag.SECURITY_AUTHENTICATION);
					}

				} catch (ExpiredJwtException e) {
					log.warn("{}만료된 토큰 발견 - 비인증 요청으로 처리", LogTag.SECURITY_JWT);
					SecurityContextHolder.clearContext();
				} catch (AuthenticationException e) {
					log.warn("{}유효하지 않은 JWT - 비인증 요청으로 처리: {}", LogTag.SECURITY_JWT, e.getMessage());
					SecurityContextHolder.clearContext();
				}
			}
		} catch (Exception e) {
			log.error("{}JWT 필터 처리 실패 - URI={}, message={}", LogTag.SECURITY_FILTER, request.getRequestURI(), e.getMessage(), e);
			SecurityContextHolder.clearContext();
		}

		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return "OPTIONS".equalsIgnoreCase(request.getMethod());
	}
}
