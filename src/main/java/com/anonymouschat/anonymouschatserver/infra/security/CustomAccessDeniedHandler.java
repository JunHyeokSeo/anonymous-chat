package com.anonymouschat.anonymouschatserver.infra.security;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.log.LogTag;
import com.anonymouschat.anonymouschatserver.web.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) throws IOException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		String principalSummary = summarizePrincipal(auth);

		// 권한 목록
		String authorities = (auth != null && auth.getAuthorities() != null)
				                     ? auth.getAuthorities().stream()
						                       .map(GrantedAuthority::getAuthority)
						                       .collect(Collectors.joining(","))
				                     : "none";

		log.warn("{}403 Forbidden - method={}, URI={}, IP={}, user={}, authorities={}, message={}",
				LogTag.SECURITY_AUTHORIZATION,
				request.getMethod(),
				request.getRequestURI(),
				request.getRemoteAddr(),
				principalSummary,
				authorities,
				ex.getMessage()
		);

		CommonResponse.writeErrorResponse(response, ErrorCode.FORBIDDEN);
	}

	private String summarizePrincipal(Authentication auth) {
		if (auth == null) return "null-authentication";

		Object principal = auth.getPrincipal();
		if (principal == null) return "null-principal";

		if (principal instanceof CustomPrincipal cp) {
			return "CustomPrincipal{userId=%s, role=%s}".formatted(cp.userId(), cp.role());
		}

		String s = principal.toString();
		int max = 120;
		return (s.length() > max) ? s.substring(0, max) + "..." : s;
	}
}
