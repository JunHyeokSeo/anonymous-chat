package com.anonymouschat.anonymouschatserver.common.jwt;

import com.anonymouschat.anonymouschatserver.common.security.OAuthPrincipal;
import com.anonymouschat.anonymouschatserver.common.util.ResponseUtil;
import com.anonymouschat.anonymouschatserver.domain.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String ATTR_PRINCIPAL = "AuthenticatedPrincipal";

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
	                                @NonNull HttpServletResponse response,
	                                @NonNull FilterChain filterChain)
			throws ServletException, IOException {

		try {
			String token = resolveToken(request);

			if (token != null && jwtTokenProvider.validateToken(token)) {
				OAuthPrincipal principal = jwtTokenProvider.getPrincipalFromToken(token);

				// request에 항상 주입 (ArgumentResolver 등에서 사용 가능)
				request.setAttribute(ATTR_PRINCIPAL, principal);

				// userId가 포함된 경우만 인증 처리
				if (principal.getUserId() != null) {
					var authentication = new UsernamePasswordAuthenticationToken(
							principal, null, List.of()
					);
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}
		} catch (JwtException e) {
			SecurityContextHolder.clearContext();
			ResponseUtil.writeUnauthorizedResponse(response, e.getMessage());
			return;
		}

		filterChain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {
		String bearer = request.getHeader("Authorization");
		if (bearer != null && bearer.startsWith("Bearer ")) {
			return bearer.substring(7);
		}
		return null;
	}

	public static OAuthPrincipal extractPrincipal(HttpServletRequest request) {
		return (OAuthPrincipal) request.getAttribute(ATTR_PRINCIPAL);
	}
}
