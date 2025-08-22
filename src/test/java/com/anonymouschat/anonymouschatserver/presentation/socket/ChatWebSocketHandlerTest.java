package com.anonymouschat.anonymouschatserver.presentation.socket;

import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.presentation.socket.support.WebSocketRateLimitGuard;
import com.anonymouschat.testsupport.security.PrincipalStub;
import com.anonymouschat.testsupport.socket.WebSocketSessionStub;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * {@link ChatWebSocketHandler}에 대한 단위 테스트 클래스입니다.
 * WebSocket 연결 수립/종료, 메시지 처리, 에러 핸들링 등 핸들러의 핵심 기능을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatWebSocketHandler 테스트")
class ChatWebSocketHandlerTest {

    @Mock private WebSocketRateLimitGuard rateLimitGuard;
    @Mock private ChatMessageDispatcher dispatcher;
    @Mock private ChatSessionManager sessionManager;
    @Mock private ObjectMapper objectMapper;

    private ChatWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ChatWebSocketHandler(rateLimitGuard, dispatcher, sessionManager, objectMapper);
    }

    /**
     * WebSocket 연결 수립 및 종료 관련 테스트
     */
    @Nested
    @DisplayName("연결 수립/종료")
    class ConnectionManagement {
        /**
         * WebSocket 연결이 성공적으로 수립되었을 때, 세션 관리자(`sessionManager`)에 세션이 올바르게 등록되는지 검증합니다.
         */
        @Test
        @DisplayName("afterConnectionEstablished: 성공적으로 연결하고 세션을 등록한다")
        void should_establish_connection_and_register_session() {
            // given
            WebSocketSession session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(100L));

            // when
            handler.afterConnectionEstablished(session);

            // then
            verify(sessionManager).registerOrReplaceSession(eq(100L), any(WebSocketSession.class));
        }

        /**
         * WebSocket 연결 수립 시 Principal(사용자 인증 정보) 추출에 실패할 때,
         * 연결이 거부되고 세션이 종료되는지 검증합니다.
         */
        @Test
        @DisplayName("afterConnectionEstablished: Principal 추출 실패 시 연결을 거부하고 세션을 닫는다")
        void should_reject_connection_when_principal_is_missing() {
            // given
            WebSocketSession session = WebSocketSessionStub.open();

            // when
            handler.afterConnectionEstablished(session);

            // then
            verify(sessionManager).forceDisconnect(any(WebSocketSession.class), any(CloseStatus.class));
            verify(sessionManager, never()).registerOrReplaceSession(anyLong(), any(WebSocketSession.class));
        }

        /**
         * WebSocket 연결이 종료된 후 호출되는 메소드(`afterConnectionClosed`)가
         * 세션 정리 및 레이트 리밋 가드 클리어 등 후속 처리를 올바르게 수행하는지 검증합니다.
         */
        @Test
        @DisplayName("afterConnectionClosed: 세션 종료 시 후속 처리를 한다")
        void should_handle_cleanup_on_connection_closed() {
            // given
            WebSocketSession session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(100L));

            // when
            handler.afterConnectionClosed(session, CloseStatus.NORMAL);

            // then
            verify(rateLimitGuard).clear(100L);
            verify(sessionManager).forceDisconnect(100L, CloseStatus.NORMAL);
        }
    }

    /**
     * WebSocket 메시지 처리 관련 테스트
     */
    @Nested
    @DisplayName("메시지 처리")
    class MessageHandling {
        /**
         * 텍스트 메시지(`TextMessage`)를 수신했을 때,
         * 메시지 디스패처(`dispatcher`)에게 처리를 올바르게 위임하는지 검증합니다.
         */
        @Test
        @DisplayName("handleTextMessage: 메시지를 dispatcher 에게 위임한다")
        void should_delegate_message_to_dispatcher() throws Exception {
            // given
            CustomPrincipal principal = (CustomPrincipal) PrincipalStub.authenticated(100L);
            WebSocketSession session = WebSocketSessionStub.withPrincipal(principal);
            ChatInboundMessage inboundMessage = new ChatInboundMessage(200L, MessageType.CHAT, "안녕하세요");
            String payload = "some json payload";

            //objectMapper가 페이로드를 인바운드 메시지로 변환하고, rateLimitGuard가 메시지 전송을 허용하도록 설정.
            when(objectMapper.readValue(payload, ChatInboundMessage.class)).thenReturn(inboundMessage);
            when(rateLimitGuard.allow(anyLong(), any())).thenReturn(true);

            // when
            handler.handleTextMessage(session, new TextMessage(payload));

            // then
            // sessionManager의 마지막 활동 시간 갱신 메소드가 호출되었는지 검증.
            verify(sessionManager).updateLastActiveAt(100L);
            // dispatcher의 dispatch 메소드가 올바른 세션과 인바운드 메시지로 호출되었는지 검증.
            verify(dispatcher).dispatch(session, inboundMessage);
        }

        /**
         * Pong 메시지를 수신했을 때,
         * 세션 관리자(`sessionManager`)의 마지막 활동 시간 갱신 메소드가 호출되는지 검증합니다.
         */
        @Test
        @DisplayName("handlePongMessage: 마지막 활동 시간을 갱신한다")
        void should_update_last_active_time_on_pong() {
            // given
            WebSocketSession session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(100L));

            // when
            handler.handlePongMessage(session, new PongMessage());

            // then
            verify(sessionManager).updateLastActiveAt(100L);
        }

        /**
         * WebSocket 전송 중 에러(`handleTransportError`)가 발생했을 때,
         * 세션이 종료(`forceDisconnect`)되는지 검증합니다.
         */
        @Test
        @DisplayName("handleTransportError: 전송 오류 발생 시 연결을 종료한다")
        void should_disconnect_on_transport_error() {
            // given
            WebSocketSession session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(100L));
            Throwable error = new IOException("Connection reset");

            // when
            handler.handleTransportError(session, error);

            // then
            verify(sessionManager).forceDisconnect(session, CloseStatus.SERVER_ERROR);
        }

        /**
         * 텍스트 메시지(`TextMessage`) 수신 시 레이트 리밋(`rateLimitGuard`)을 초과하여
         * 메시지 전송이 허용되지 않을 때,
         * 세션이 `POLICY_VIOLATION` 상태로 종료되고 메시지 디스패처(`dispatcher`)가 호출되지 않는지 검증합니다.
         */
        @Test
        @DisplayName("handleTextMessage: 레이트 리밋 초과 시 연결을 종료하고 메시지를 처리하지 않는다")
        void should_disconnect_and_not_dispatch_when_rate_limit_exceeded() throws Exception {
            // given
            CustomPrincipal principal = (CustomPrincipal) PrincipalStub.authenticated(100L);
            WebSocketSession session = WebSocketSessionStub.withPrincipal(principal);
            ChatInboundMessage inboundMessage = new ChatInboundMessage(200L, MessageType.CHAT, "안녕하세요");
            String payload = "some json payload";

            when(objectMapper.readValue(payload, ChatInboundMessage.class)).thenReturn(inboundMessage);
	        // rateLimitGuard가 메시지 전송을 허용하지 않도록 false 반환.
	        when(rateLimitGuard.allow(anyLong(), any())).thenReturn(false);

            // when
            handler.handleTextMessage(session, new TextMessage(payload));

            // then
            verify(sessionManager).updateLastActiveAt(100L);
            // 메시지 디스패처는 호출되지 않았는지 검증.
            verify(dispatcher, never()).dispatch(any(), any());
        }
    }
}