package com.anonymouschat.anonymouschatserver.infra.security.jwt;

import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFactory {

	private final JwtTokenProvider jwtTokenProvider;

	public CustomPrincipal createPrincipal(String token) {
		CustomPrincipal principal = jwtTokenProvider.getPrincipalFromToken(token);
		log.debug("{}Principal 생성 완료 - userId={}, provider={}, role={}, authenticated={}",
				LogTag.SECURITY_JWT,
				principal.userId(),
				principal.provider(),
				principal.role(),
				principal.isAuthenticatedUser()
		);
		return principal;
	}

	public Authentication createAuthentication(CustomPrincipal principal) {
		var authority = principal.role(); // 항상 Role 있음 (USER, ADMIN, GUEST)

		log.debug("{}Authentication 객체 생성 - userId={}, role={}",
				LogTag.SECURITY_JWT,
				principal.userId(),
				principal.role()
		);

		return new UsernamePasswordAuthenticationToken(principal, null, List.of(authority));
	}
}
