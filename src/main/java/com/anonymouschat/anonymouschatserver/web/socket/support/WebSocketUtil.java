package com.anonymouschat.anonymouschatserver.web.socket.support;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.UnauthorizedException;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket 세션에서 사용자 인증 정보(Principal)를 추출하는 유틸리티 클래스입니다.
 * 세션 속성에서 CustomPrincipal 객체를 안전하게 가져오고, 사용자 ID를 추출하는 기능을 제공합니다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebSocketUtil {
	private static final String ATTR_PRINCIPAL = "principal";

	/**
	 * WebSocket 세션에서 CustomPrincipal 객체를 추출합니다.
	 * 세션에 인증 정보가 없거나, 인증되지 않은 사용자일 경우 IllegalStateException을 발생시킵니다.
	 *
	 * @param session CustomPrincipal을 추출할 WebSocketSession
	 * @return 추출된 CustomPrincipal 객체
	 * @throws IllegalStateException 세션에 인증 정보가 없거나, 인증되지 않은 사용자일 경우
	 */	public static CustomPrincipal extractPrincipal(WebSocketSession session) {
		Object attr = session.getAttributes().get(ATTR_PRINCIPAL);
		if (attr instanceof CustomPrincipal principal) {
			if (!principal.isAuthenticatedUser()) {
				throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
			}
			return principal;
		}
		throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
	}

	/**
	 * WebSocket 세션에서 사용자 ID를 추출합니다.
	 * {@link #extractPrincipal(WebSocketSession)}을 호출하여 Principal을 먼저 추출합니다.
	 *
	 * @param session 사용자 ID를 추출할 WebSocketSession
	 * @return 추출된 사용자 ID
	 * @throws IllegalStateException 세션에 인증 정보가 없거나, 인증되지 않은 사용자일 경우
	 */	public static Long extractUserId(WebSocketSession session) {
		return extractPrincipal(session).userId();
	}
}