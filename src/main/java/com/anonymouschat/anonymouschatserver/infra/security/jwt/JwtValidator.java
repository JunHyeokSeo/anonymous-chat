package com.anonymouschat.anonymouschatserver.infra.security.jwt;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtValidator {

	private final JwtTokenProvider jwtTokenProvider;

	public void validate(String token) {
		if (!jwtTokenProvider.validateToken(token)) {
			throw new InvalidTokenException(ErrorCode.INVALID_TOKEN);
		}
	}
}