package com.anonymouschat.anonymouschatserver.infra.security;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.auth.UnsupportedAlgorithmAuthenticationException;
import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import com.anonymouschat.anonymouschatserver.web.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

import static com.anonymouschat.anonymouschatserver.common.code.ErrorCode.*;

@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
		ErrorCode errorCode = resolveErrorCode(authException);

		log.warn("{}인증 실패 - URI={}, code={}, message={}", LogTag.SECURITY_ENTRYPOINT, request.getRequestURI(), errorCode.name(), errorCode.getMessage());

		CommonResponse.writeErrorResponse(response, errorCode);
	}

	private ErrorCode resolveErrorCode(AuthenticationException ex) {
		// 스프링 시큐리티 예외
		if (ex instanceof CredentialsExpiredException)         return EXPIRED_TOKEN;
		if (ex instanceof UnsupportedAlgorithmAuthenticationException) return UNSUPPORTED_ALGORITHM;
		if (ex instanceof org.springframework.security.authentication.AuthenticationServiceException) return UNEXPECTED_AUTH_ERROR;

		// BadCredentials인 경우에만 cause 한 번 훑기
		if (ex instanceof org.springframework.security.authentication.BadCredentialsException) {
			Throwable c = ex.getCause();
			if (c instanceof io.jsonwebtoken.ExpiredJwtException)        return EXPIRED_TOKEN;
			if (c instanceof io.jsonwebtoken.UnsupportedJwtException)    return UNSUPPORTED_TOKEN;
			if (c instanceof io.jsonwebtoken.MalformedJwtException)      return MALFORMED_TOKEN;
			if (c instanceof io.jsonwebtoken.security.SecurityException
					    || c instanceof java.security.SignatureException
					    || c instanceof SecurityException)                          return INVALID_SIGNATURE;
			// 그 밖의 BadCredentials
			return INVALID_TOKEN;
		}

		return UNAUTHORIZED;
	}


}
