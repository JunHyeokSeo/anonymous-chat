package com.anonymouschat.anonymouschatserver.common.socket.support;

import com.anonymouschat.anonymouschatserver.common.security.CustomPrincipal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebSocketUtil {
	private static final String ATTR_PRINCIPAL = "principal";

	public static CustomPrincipal extractPrincipal(WebSocketSession session) {
		Object attr = session.getAttributes().get(ATTR_PRINCIPAL);
		if (attr instanceof CustomPrincipal principal) {
			if (!principal.isAuthenticatedUser()) {
				throw new IllegalStateException("인증되지 않은 사용자는 메시지를 보낼 수 없습니다.");
			}
			return principal;
		}
		throw new IllegalStateException("WebSocket 세션에 인증 정보가 없습니다.");
	}

	public static Long extractUserId(WebSocketSession session) {
		return extractPrincipal(session).userId();
	}
}
