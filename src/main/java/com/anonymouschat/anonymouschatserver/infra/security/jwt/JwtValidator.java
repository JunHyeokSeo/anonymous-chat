package com.anonymouschat.anonymouschatserver.infra.security.jwt;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;
import com.anonymouschat.anonymouschatserver.common.log.LogTag;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtValidator {

	private final JwtTokenProvider jwtTokenProvider;

	public void validate(String token) {
		try {
			Jws<Claims> parsedToken = Jwts.parserBuilder()
					                        .setSigningKey(jwtTokenProvider.getSecretKey())
					                        .build()
					                        .parseClaimsJws(token);

			JwsHeader<?> header = parsedToken.getHeader();
			if (!SignatureAlgorithm.HS256.getValue().equals(header.getAlgorithm())) {
                log.warn("{}Invalid algorithm: {}", LogTag.SECURITY, header.getAlgorithm());
				throw new InvalidTokenException(ErrorCode.INVALID_TOKEN);
			}
		} catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            log.warn("{}reason={}, token={}", LogTag.SECURITY, e.getMessage(), token);
			throw new InvalidTokenException(ErrorCode.INVALID_TOKEN, e);
		} catch (ExpiredJwtException e) {
            log.warn("{}Expired token: {}", LogTag.SECURITY, token);
			throw new InvalidTokenException(ErrorCode.EXPIRED_TOKEN, e);
		}
	}
}
