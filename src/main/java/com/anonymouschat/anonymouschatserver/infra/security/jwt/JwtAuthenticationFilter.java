package com.anonymouschat.anonymouschatserver.infra.security.jwt;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.log.LogTag;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.web.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
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
	                                @NonNull FilterChain filterChain) throws IOException {

		try {
			String token = tokenResolver.resolve(request);

			if (token != null) {
				log.debug("{}JWT 토큰 감지됨 - token={}", LogTag.SECURITY_JWT, token);

				jwtValidator.validate(token);
				log.debug("{}JWT 유효성 검사 통과", LogTag.SECURITY_JWT);

				CustomPrincipal principal = authFactory.createPrincipal(token);
				log.debug("{}Principal 생성 완료 - userId={}, role={}", LogTag.SECURITY_AUTHENTICATION,
						principal.userId(), principal.role());

				// 항상 request에 주입
				request.setAttribute(ATTR_PRINCIPAL, principal);
				log.trace("{}Request에 Principal 주입 완료", LogTag.SECURITY_FILTER);

				// 기존 컨텍스트 제거
				SecurityContextHolder.clearContext();

				var authentication = authFactory.createAuthentication(principal);
				if (authentication != null) {
					SecurityContext context = SecurityContextHolder.createEmptyContext();
					context.setAuthentication(authentication);
					SecurityContextHolder.setContext(context);
					log.debug("{}SecurityContext에 Authentication 설정 완료", LogTag.SECURITY_AUTHENTICATION);
				}
			} else {
				log.trace("{}요청에 JWT 토큰 없음 - URI={}", LogTag.SECURITY_FILTER, request.getRequestURI());
			}

			filterChain.doFilter(request, response);

		} catch (Exception e) {
			log.error("{}JWT 필터 처리 실패 - URI={}, message={}", LogTag.SECURITY_FILTER, request.getRequestURI(), e.getMessage(), e);
			SecurityContextHolder.clearContext();
			ApiResponse.writeErrorResponse(response, ErrorCode.UNAUTHORIZED);
		}
	}

	public static CustomPrincipal extractPrincipal(HttpServletRequest request) {
		return (CustomPrincipal) request.getAttribute(ATTR_PRINCIPAL);
	}
}