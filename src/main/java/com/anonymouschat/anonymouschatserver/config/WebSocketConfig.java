package com.anonymouschat.anonymouschatserver.config;

import com.anonymouschat.anonymouschatserver.web.socket.ChatWebSocketHandler;
import com.anonymouschat.anonymouschatserver.web.socket.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 엔드포인트 및 인터셉터를 등록하는 설정 클래스입니다.
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
	private final ChatWebSocketHandler chatWebSocketHandler;
	private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

	/**
	 * WebSocket 엔드포인트를 등록하고, 핸드셰이크 인터셉터를 설정합니다.
	 *
	 * @param registry WebSocketHandlerRegistry
	 */
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(chatWebSocketHandler, "/ws/chat")
				.addInterceptors(jwtHandshakeInterceptor)
				.setAllowedOrigins("*") //todo: 특정 Origin만 허용 하도록 변경 예정
				.withSockJS();
	}
}
