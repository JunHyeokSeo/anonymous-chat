package com.anonymouschat.anonymouschatserver.web.socket.handler;

import com.anonymouschat.anonymouschatserver.application.event.ChatSave;
import com.anonymouschat.anonymouschatserver.web.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatOutboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.web.socket.support.MessageBroadcaster;
import com.anonymouschat.anonymouschatserver.web.socket.support.WebSocketAccessGuard;
import com.anonymouschat.testsupport.security.PrincipalStub;
import com.anonymouschat.testsupport.socket.WebSocketSessionStub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMessageHandler 테스트")
class ChatMessageHandlerTest {

    @Mock private ChatSessionManager sessionManager;
    @Mock private ApplicationEventPublisher publisher;
    @Mock private MessageBroadcaster broadcaster;
    @Mock private WebSocketAccessGuard guard;
    @InjectMocks private ChatMessageHandler handler;

    @Captor private ArgumentCaptor<ChatSave> chatSaveEventCaptor;
    @Captor private ArgumentCaptor<ChatOutboundMessage> outboundMessageCaptor;

    @Test
    @DisplayName("참여자가 보낸 메시지를 이벤트로 발행하고 브로드캐스트한다")
    void should_publish_event_and_broadcast_message_from_participant() {
        // given
        long roomId = 100L, senderId = 1L;
        var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(senderId));
        var inbound = new ChatInboundMessage(roomId, MessageType.CHAT, "안녕하세요");

        when(guard.ensureParticipant(session, roomId, senderId)).thenReturn(true);

        // when
        handler.handle(session, inbound);

        // then
        verify(publisher).publishEvent(chatSaveEventCaptor.capture());
        ChatSave capturedEvent = chatSaveEventCaptor.getValue();
        assertThat(capturedEvent.roomId()).isEqualTo(roomId);
        assertThat(capturedEvent.senderId()).isEqualTo(senderId);
        assertThat(capturedEvent.content()).isEqualTo("안녕하세요");

        verify(broadcaster).broadcast(eq(roomId), outboundMessageCaptor.capture());
        ChatOutboundMessage capturedMessage = outboundMessageCaptor.getValue();
        assertThat(capturedMessage.roomId()).isEqualTo(roomId);
        assertThat(capturedMessage.senderId()).isEqualTo(senderId);
        assertThat(capturedMessage.content()).isEqualTo("안녕하세요");
    }

    @Test
    @DisplayName("참여자가 아니면 메시지를 처리하지 않는다")
    void should_not_handle_message_if_not_a_participant() {
        // given
        long roomId = 100L, senderId = 1L;
        var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(senderId));
        var inbound = new ChatInboundMessage(roomId, MessageType.CHAT, "안녕하세요");

        when(guard.ensureParticipant(session, roomId, senderId)).thenReturn(false);

        // when
        handler.handle(session, inbound);

        // then
        verify(publisher, never()).publishEvent(any());
        verify(broadcaster, never()).broadcast(anyLong(), any());
    }

    @Test
    @DisplayName("처리 중 예외 발생 시 세션을 종료한다")
    void should_close_session_on_exception() {
        // given
        long roomId = 100L, senderId = 1L;
        var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(senderId));
        var inbound = new ChatInboundMessage(roomId, MessageType.CHAT, "안녕하세요");

        when(guard.ensureParticipant(session, roomId, senderId)).thenReturn(true);
        doThrow(new RuntimeException("Broadcast failed")).when(broadcaster).broadcast(anyLong(), any());

        // when
        handler.handle(session, inbound);

        // then
        verify(sessionManager).forceDisconnect(eq(session), any());
    }
}
