package com.anonymouschat.anonymouschatserver.infra.security.jwt;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtValidator {

	private final JwtTokenProvider jwtTokenProvider;

	public void validate(String token) throws JwtException {
		if (!jwtTokenProvider.validateToken(token)) {
			throw new JwtException("유효하지 않은 JWT 입니다.");
		}
	}
}

