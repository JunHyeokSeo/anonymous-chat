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
			log.debug("{}JWT 파싱 시작 - token={}", LogTag.SECURITY_JWT, token);

			Jws<Claims> parsedToken = Jwts.parserBuilder()
					                          .setSigningKey(jwtTokenProvider.getSecretKey())
					                          .build()
					                          .parseClaimsJws(token);

			JwsHeader<?> header = parsedToken.getHeader();
			if (!SignatureAlgorithm.HS256.getValue().equals(header.getAlgorithm())) {
				log.warn("{}알고리즘 불일치 - provided={}, expected={}", LogTag.SECURITY_JWT, header.getAlgorithm(), SignatureAlgorithm.HS256.getValue());
				throw new InvalidTokenException(ErrorCode.INVALID_TOKEN);
			}

			log.debug("{}JWT 유효성 검사 통과", LogTag.SECURITY_JWT);

		} catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
			log.warn("{}JWT 파싱 오류 - reason={}, token={}", LogTag.SECURITY_JWT, e.getMessage(), token);
			throw new InvalidTokenException(ErrorCode.INVALID_TOKEN, e);
		} catch (ExpiredJwtException e) {
			log.warn("{}만료된 토큰 사용 시도 - token={}", LogTag.SECURITY_JWT, token);
			throw new InvalidTokenException(ErrorCode.EXPIRED_TOKEN, e);
		}
	}
}
