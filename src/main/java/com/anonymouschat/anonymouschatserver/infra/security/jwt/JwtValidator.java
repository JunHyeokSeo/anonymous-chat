package com.anonymouschat.anonymouschatserver.infra.security.jwt;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.auth.UnsupportedAlgorithmAuthenticationException;
import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtValidator {

	private final JwtTokenProvider jwtTokenProvider;

	public void validate(String token) throws AuthenticationException {
		try {
			log.debug("{}JWT 파싱 시작 - token={}", LogTag.SECURITY_JWT, token);

			Jws<Claims> parsedToken = Jwts.parserBuilder()
					                          .setSigningKey(jwtTokenProvider.getSecretKey())
					                          .setAllowedClockSkewSeconds(60)
					                          .build()
					                          .parseClaimsJws(token);

			JwsHeader<?> header = parsedToken.getHeader();
			if (!SignatureAlgorithm.HS256.getValue().equals(header.getAlgorithm())) {
				log.warn("{}알고리즘 불일치 - provided={}, expected={}", LogTag.SECURITY_JWT, header.getAlgorithm(), SignatureAlgorithm.HS256.getValue());
				throw new UnsupportedAlgorithmAuthenticationException(ErrorCode.UNSUPPORTED_ALGORITHM.getMessage());
			}

			log.debug("{}JWT 유효성 검사 통과", LogTag.SECURITY_JWT);

		} catch (ExpiredJwtException e) {
			log.warn("{}만료된 토큰 사용 시도 - token={}", LogTag.SECURITY_JWT, token);
			throw new CredentialsExpiredException(ErrorCode.EXPIRED_TOKEN.getMessage(), e);

		} catch (UnsupportedJwtException e) {
			log.warn("{}지원하지 않는 JWT 포맷 - token={}", LogTag.SECURITY_JWT, token);
			throw new BadCredentialsException(ErrorCode.UNSUPPORTED_TOKEN.getMessage(), e);

		} catch (MalformedJwtException e) {
			log.warn("{}비정상 JWT 포맷 - token={}", LogTag.SECURITY_JWT, token);
			throw new BadCredentialsException(ErrorCode.MALFORMED_TOKEN.getMessage(), e);

		} catch (SecurityException e) {
			log.warn("{}서명 검증 실패 - token={}", LogTag.SECURITY_JWT, token);
			throw new BadCredentialsException(ErrorCode.INVALID_SIGNATURE.getMessage(), e);

		} catch (IllegalArgumentException e) {
			log.warn("{}잘못된 JWT 인자 - token={}", LogTag.SECURITY_JWT, token);
			throw new BadCredentialsException(ErrorCode.INVALID_TOKEN.getMessage(), e);
		}
	}
}
