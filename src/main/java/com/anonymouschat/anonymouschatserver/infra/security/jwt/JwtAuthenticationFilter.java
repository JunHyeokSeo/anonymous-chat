package com.anonymouschat.anonymouschatserver.infra.security.jwt;

import com.anonymouschat.anonymouschatserver.common.log.LogTag;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.anonymouschat.anonymouschatserver.common.code.ErrorCode.UNEXPECTED_AUTH_ERROR;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String ATTR_PRINCIPAL = "AuthenticatedPrincipal";

	private final JwtTokenResolver tokenResolver;
	private final JwtValidator jwtValidator;
	private final JwtAuthenticationFactory authFactory;
	private final AuthenticationEntryPoint entryPoint;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
	                                @NonNull HttpServletResponse response,
	                                @NonNull FilterChain filterChain) throws IOException, ServletException {

		// 토큰 처리(추출/검증/컨텍스트 세팅)만 try-catch로 감싼다
		try {
			String token = tokenResolver.resolve(request);

			if (token == null) {
				log.trace("{}요청에 JWT 토큰 없음 - URI={}", LogTag.SECURITY_FILTER, request.getRequestURI());
				// 토큰이 없으면 그냥 다음 필터로 넘김 (permitAll 보호)
			} else {
				log.debug("{}JWT 토큰 감지됨 - token={}", LogTag.SECURITY_JWT, token);

				jwtValidator.validate(token);  // 여기서 AuthenticationException 파생 가능
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
			}

		} catch (AuthenticationException e) {
			// 인증 실패는 그대로 던져 ETF가 EntryPoint 호출하게 함
			log.warn("인증 실패: {}", e.getMessage());
			SecurityContextHolder.clearContext();
			entryPoint.commence(request, response, e);
			return;
		} catch (Exception e) {
			// 서버 사이드 예기치 못한 오류 → EntryPoint로 유도 (401 처리)
			log.error("{}JWT 필터 처리 실패 - URI={}, message={}", LogTag.SECURITY_FILTER, request.getRequestURI(), e.getMessage(), e);
			SecurityContextHolder.clearContext();
			AuthenticationException ae = new org.springframework.security.authentication.AuthenticationServiceException(
					com.anonymouschat.anonymouschatserver.common.code.ErrorCode.UNEXPECTED_AUTH_ERROR.getMessage(), e);
			entryPoint.commence(request, response, ae);
			return;
		}

		filterChain.doFilter(request, response);
	}


	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String uri = request.getRequestURI();
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true; // CORS preflight
		return uri.startsWith("/api/v1/auth/") || uri.startsWith("/oauth2/");
	}

	public static CustomPrincipal extractPrincipal(HttpServletRequest request) {
		return (CustomPrincipal) request.getAttribute(ATTR_PRINCIPAL);
	}
}
