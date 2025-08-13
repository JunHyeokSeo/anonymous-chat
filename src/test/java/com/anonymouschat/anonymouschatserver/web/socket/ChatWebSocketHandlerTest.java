package com.anonymouschat.anonymouschatserver.web.socket;

import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.web.socket.support.WebSocketRateLimitGuard;
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

    @Nested
    @DisplayName("연결 수립/종료")
    class ConnectionManagement {
        @Test
        @DisplayName("afterConnectionEstablished: 성공적으로 연결하고 세션을 등록한다")
        void should_establish_connection_and_register_session() throws Exception {
            // given
            WebSocketSession session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(100L));

            // when
            handler.afterConnectionEstablished(session);

            // then
            verify(sessionManager).registerOrReplaceSession(eq(100L), any(WebSocketSession.class));
        }

        @Test
        @DisplayName("afterConnectionEstablished: Principal 추출 실패 시 연결을 거부하고 세션을 닫는다")
        void should_reject_connection_when_principal_is_missing() throws Exception {
            // given
            WebSocketSession session = WebSocketSessionStub.open(); // Principal 없음

            // when
            handler.afterConnectionEstablished(session);

            // then
            verify(sessionManager).forceDisconnect(any(WebSocketSession.class), any(CloseStatus.class));
            verify(sessionManager, never()).registerOrReplaceSession(anyLong(), any(WebSocketSession.class));
        }

        @Test
        @DisplayName("afterConnectionClosed: 세션 종료 시 후속 처리를 한다")
        void should_handle_cleanup_on_connection_closed() throws Exception {
            // given
            WebSocketSession session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(100L));

            // when
            handler.afterConnectionClosed(session, CloseStatus.NORMAL);

            // then
            verify(rateLimitGuard).clear(100L);
            verify(sessionManager).forceDisconnect(100L, CloseStatus.NORMAL);
        }
    }

    @Nested
    @DisplayName("메시지 처리")
    class MessageHandling {
        @Test
        @DisplayName("handleTextMessage: 메시지를 dispatcher에게 위임한다")
        void should_delegate_message_to_dispatcher() throws Exception {
            // given
            CustomPrincipal principal = (CustomPrincipal) PrincipalStub.authenticated(100L);
            WebSocketSession session = WebSocketSessionStub.withPrincipal(principal);
            ChatInboundMessage inboundMessage = new ChatInboundMessage(200L, MessageType.CHAT, "안녕하세요");
            String payload = "some json payload";

            when(objectMapper.readValue(payload, ChatInboundMessage.class)).thenReturn(inboundMessage);
            when(rateLimitGuard.allow(anyLong(), any())).thenReturn(true);

            // when
            handler.handleTextMessage(session, new TextMessage(payload));

            // then
            verify(sessionManager).updateLastActiveAt(100L);
            verify(dispatcher).dispatch(session, inboundMessage);
        }

        @Test
        @DisplayName("handlePongMessage: 마지막 활동 시간을 갱신한다")
        void should_update_last_active_time_on_pong() throws Exception {
            // given
            WebSocketSession session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(100L));

            // when
            handler.handlePongMessage(session, new PongMessage());

            // then
            verify(sessionManager).updateLastActiveAt(100L);
        }

        @Test
        @DisplayName("handleTransportError: 전송 오류 발생 시 연결을 종료한다")
        void should_disconnect_on_transport_error() throws Exception {
            // given
            WebSocketSession session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(100L));
            Throwable error = new IOException("Connection reset");

            // when
            handler.handleTransportError(session, error);

            // then
            verify(sessionManager).forceDisconnect(session, CloseStatus.SERVER_ERROR);
        }

        @Test
        @DisplayName("handleTextMessage: 레이트 리밋 초과 시 연결을 종료하고 메시지를 처리하지 않는다")
        void should_disconnect_and_not_dispatch_when_rate_limit_exceeded() throws Exception {
            // given
            CustomPrincipal principal = (CustomPrincipal) PrincipalStub.authenticated(100L);
            WebSocketSession session = WebSocketSessionStub.withPrincipal(principal);
            ChatInboundMessage inboundMessage = new ChatInboundMessage(200L, MessageType.CHAT, "안녕하세요");
            String payload = "some json payload";

            when(objectMapper.readValue(payload, ChatInboundMessage.class)).thenReturn(inboundMessage);
            when(rateLimitGuard.allow(anyLong(), any())).thenReturn(false); // Simulate rate limit exceeded

            // when
            handler.handleTextMessage(session, new TextMessage(payload));

            // then
            verify(sessionManager).updateLastActiveAt(100L); // Still updates last active time
            verify(sessionManager).forceDisconnect(session, CloseStatus.POLICY_VIOLATION);
            verify(dispatcher, never()).dispatch(any(), any()); // Dispatcher should not be called
        }
    }
}