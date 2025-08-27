package com.anonymouschat.anonymouschatserver.infra.security;

import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import com.anonymouschat.anonymouschatserver.presentation.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

import static com.anonymouschat.anonymouschatserver.common.code.ErrorCode.UNAUTHORIZED;

@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request,
	                     HttpServletResponse response,
	                     AuthenticationException authException) throws IOException {

		log.warn("{}인증 실패 - URI={}, message={}", LogTag.SECURITY_ENTRYPOINT, request.getRequestURI(), authException.getMessage());

		CommonResponse.writeErrorResponse(response, UNAUTHORIZED);
	}
}
