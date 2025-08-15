package com.anonymouschat.anonymouschatserver.infra.security.jwt;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtValidator {

	private final JwtTokenProvider jwtTokenProvider;

	public void validate(String token) {
		try {
			Jwts.parserBuilder()
					.setSigningKey(jwtTokenProvider.getSecretKey())
					.build()
					.parseClaimsJws(token);
		} catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
			throw new InvalidTokenException(ErrorCode.INVALID_TOKEN, e);
		} catch (ExpiredJwtException e) {
			throw new InvalidTokenException(ErrorCode.EXPIRED_TOKEN, e);
		}
	}
}
