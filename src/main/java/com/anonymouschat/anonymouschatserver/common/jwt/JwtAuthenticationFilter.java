package com.anonymouschat.anonymouschatserver.common.jwt;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.security.OAuthPrincipal;
import com.anonymouschat.anonymouschatserver.common.security.PrincipalType;
import com.anonymouschat.anonymouschatserver.common.security.UserPrincipal;
import com.anonymouschat.anonymouschatserver.common.util.ResponseUtil;
import com.anonymouschat.anonymouschatserver.domain.user.entity.User;
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

				// ✅ 모든 principal은 request에 주입
				request.setAttribute(ATTR_PRINCIPAL, principal);

				// ✅ ACCESS principal만 Spring Security 인증 처리
				if (principal.getType() == PrincipalType.ACCESS) {
					if (!(principal instanceof UserPrincipal userPrincipal)) {
						throw new JwtException("ACCESS 토큰의 Principal 타입이 올바르지 않습니다.");
					}

					User user = userRepository.findById(userPrincipal.userId())
							            .orElseThrow(() -> new IllegalStateException(ErrorCode.UNAUTHORIZED.getMessage()));

					var authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
				// REGISTRATION은 인증 스킵 (단, request에는 주입함 → ArgumentResolver에서 처리)
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
