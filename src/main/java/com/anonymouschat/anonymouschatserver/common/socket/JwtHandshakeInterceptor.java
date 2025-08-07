package com.anonymouschat.anonymouschatserver.common.socket;

import com.anonymouschat.anonymouschatserver.common.jwt.JwtAuthenticationFactory;
import com.anonymouschat.anonymouschatserver.common.jwt.JwtTokenResolver;
import com.anonymouschat.anonymouschatserver.common.jwt.JwtValidator;
import com.anonymouschat.anonymouschatserver.common.security.CustomPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 핸드셰이크 요청 시 JWT 토큰을 검증하고,
 * 인증된 사용자 정보를 WebSocket 세션에 주입하는 인터셉터입니다.
 */
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

	private final JwtTokenResolver tokenResolver;
	private final JwtValidator jwtValidator;
	private final JwtAuthenticationFactory authFactory;

	/**
	 * WebSocket 연결 전에 JWT 토큰을 검증하고, principal 정보를 세션에 저장합니다.
	 *
	 * @param request     WebSocket handshake 요청
	 * @param response    WebSocket handshake 응답
	 * @param wsHandler   WebSocket handler
	 * @param attributes  WebSocket 세션 attribute 저장소
	 * @return 연결 허용 여부 (유효하지 않은 토큰일 경우 false)
	 */
	@Override
	public boolean beforeHandshake(@NonNull ServerHttpRequest request,
	                               @NonNull ServerHttpResponse response,
	                               @NonNull WebSocketHandler wsHandler,
	                               @NonNull Map<String, Object> attributes) {

		try {
			String token = tokenResolver.resolve(request);
			jwtValidator.validate(token);
			CustomPrincipal principal = authFactory.createPrincipal(token);

			//인증된 사용자만 연결 허용
			if (!principal.isAuthenticatedUser()) {
				response.setStatusCode(HttpStatus.UNAUTHORIZED);
				return false;
			}

			attributes.put("principal", principal);
			return true;

		} catch (Exception e) {
			// 연결 거부: 클라이언트에게 401로 응답 (WebSocket은 HTTP 기반 handshake)
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return false;
		}
	}

	@Override
	public void afterHandshake(@NonNull ServerHttpRequest request,
	                           @NonNull ServerHttpResponse response,
	                           @NonNull WebSocketHandler wsHandler,
	                           Exception exception) {
	}
}
