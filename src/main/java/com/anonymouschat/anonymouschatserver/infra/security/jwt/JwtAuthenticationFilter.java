package com.anonymouschat.anonymouschatserver.infra.security.jwt;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.web.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
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
				jwtValidator.validate(token);
				CustomPrincipal principal = authFactory.createPrincipal(token);

				// 항상 request에 주입
				request.setAttribute(ATTR_PRINCIPAL, principal);

				// 기존 컨텍스트 제거
				SecurityContextHolder.clearContext();

				// 인증된 사용자만 SecurityContext에 주입
				var authentication = authFactory.createAuthentication(principal);
				if (authentication != null) {
					// 새로운 인증 정보 설정
					SecurityContext context = SecurityContextHolder.createEmptyContext();
					context.setAuthentication(authentication);
					SecurityContextHolder.setContext(context);
				}
			}

			filterChain.doFilter(request, response);
		} catch (Exception e) {
			SecurityContextHolder.clearContext();
			ApiResponse.writeErrorResponse(response, ErrorCode.UNAUTHORIZED, e.getMessage());
		}
	}

	public static CustomPrincipal extractPrincipal(HttpServletRequest request) {
		return (CustomPrincipal) request.getAttribute(ATTR_PRINCIPAL);
	}
}