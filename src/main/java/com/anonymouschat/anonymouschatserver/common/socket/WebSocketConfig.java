package com.anonymouschat.anonymouschatserver.common.socket;

import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.jwt.JwtAuthenticationFactory;
import com.anonymouschat.anonymouschatserver.common.jwt.JwtTokenResolver;
import com.anonymouschat.anonymouschatserver.common.jwt.JwtValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
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
	private final ApplicationEventPublisher applicationEventPublisher;
	private final ObjectMapper objectMapper;
	private final ChatSessionManager chatSessionManager;
	private final UserService userService;
	private final JwtTokenResolver tokenResolver;
	private final JwtValidator jwtValidator;
	private final JwtAuthenticationFactory authFactory;

	/**
	 * WebSocket 엔드포인트를 등록하고, 핸드셰이크 인터셉터를 설정합니다.
	 *
	 * @param registry WebSocketHandlerRegistry
	 */
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(chatWebSocketHandler(), "/ws/chat")
				.addInterceptors(jwtHandshakeInterceptor())
				.setAllowedOrigins("*") //todo: 특정 Origin만 허용 하도록 변경 예정
				.withSockJS();
	}

	@Bean
	public ChatWebSocketHandler chatWebSocketHandler() {
		return new ChatWebSocketHandler(applicationEventPublisher, chatSessionManager, objectMapper, userService);
	}

	@Bean
	public JwtHandshakeInterceptor jwtHandshakeInterceptor() {
		return new JwtHandshakeInterceptor(tokenResolver, jwtValidator, authFactory);
	}
}
