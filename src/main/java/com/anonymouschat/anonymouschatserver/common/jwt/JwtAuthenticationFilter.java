package com.anonymouschat.anonymouschatserver.common.jwt;

import com.anonymouschat.anonymouschatserver.domain.user.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.user.User;
import com.anonymouschat.anonymouschatserver.domain.user.UserRepository;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.util.ResponseUtil;
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

		String token = resolveToken(request);

		try {
			if (token != null && jwtTokenProvider.validateToken(token)) {
				JwtUserInfo userInfo = jwtTokenProvider.getUserInfoFromToken(token);
				OAuthProvider provider = userInfo.provider();
				String providerId = userInfo.providerId();

				User user = userRepository.findByProviderAndProviderId(provider, providerId)
						            .orElseThrow(() -> new IllegalStateException(ErrorCode.UNAUTHORIZED.getMessage()));

				UsernamePasswordAuthenticationToken authentication =
						new UsernamePasswordAuthenticationToken(user, null, List.of());

				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (JwtException e) {
			SecurityContextHolder.clearContext();
			ResponseUtil.writeUnauthorizedResponse(response, e.getMessage());
			return; // 필수
		}

		filterChain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");

		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7); // "Bearer " 이후
		}
		return null;
	}
}
