package com.anonymouschat.anonymouschatserver.common.jwt;

import com.anonymouschat.anonymouschatserver.common.security.CustomPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFactory {

	private final JwtTokenProvider jwtTokenProvider;

	public CustomPrincipal createPrincipal(String token) {
		return jwtTokenProvider.getPrincipalFromToken(token);
	}

	public Authentication createAuthentication(CustomPrincipal principal) {
		if (!principal.isAuthenticatedUser()) return null;

		var authority = new SimpleGrantedAuthority(principal.role());
		return new UsernamePasswordAuthenticationToken(principal, null, List.of(authority));
	}
}

