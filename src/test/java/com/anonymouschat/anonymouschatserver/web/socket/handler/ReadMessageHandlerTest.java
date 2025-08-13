package com.anonymouschat.anonymouschatserver.web.socket.handler;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.usecase.MessageUseCase;
import com.anonymouschat.anonymouschatserver.web.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatInboundMessage;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReadMessageHandler 테스트")
class ReadMessageHandlerTest {

    @Mock private ChatSessionManager sessionManager;
    @Mock private MessageUseCase messageUseCase;
    @Mock private MessageBroadcaster broadcaster;
    @Mock private WebSocketAccessGuard guard;
    @InjectMocks private ReadMessageHandler handler;

    @Captor private ArgumentCaptor<MessageUseCaseDto.MarkMessagesAsRead> captor;

    @Test
    @DisplayName("참여자가 보낸 읽음 메시지를 처리하고, 그 결과를 브로드캐스트한다")
    void should_mark_as_read_and_broadcast_on_message_from_participant() {
        // given
        long roomId = 100L, userId = 1L;
        var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(userId));
        var inbound = new ChatInboundMessage(roomId, MessageType.READ, null);

        when(guard.ensureParticipant(session, roomId, userId)).thenReturn(true);
        when(messageUseCase.markMessagesAsRead(any())).thenReturn(999L);

        // when
        handler.handle(session, inbound);

        // then
        verify(messageUseCase).markMessagesAsRead(captor.capture());
        MessageUseCaseDto.MarkMessagesAsRead captured = captor.getValue();
        assertThat(captured.roomId()).isEqualTo(roomId);
        assertThat(captured.userId()).isEqualTo(userId);

        verify(broadcaster).broadcastExcept(eq(roomId), any(), eq(userId));
    }

    @Test
    @DisplayName("읽음 처리할 메시지가 없으면 브로드캐스트하지 않는다")
    void should_not_broadcast_if_no_message_was_read() {
        // given
        long roomId = 100L, userId = 1L;
        var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(userId));
        var inbound = new ChatInboundMessage(roomId, MessageType.READ, null);

        when(guard.ensureParticipant(session, roomId, userId)).thenReturn(true);
        when(messageUseCase.markMessagesAsRead(any())).thenReturn(null);

        // when
        handler.handle(session, inbound);

        // then
        verify(broadcaster, never()).broadcastExcept(anyLong(), any(), anyLong());
    }

    @Test
    @DisplayName("참여자가 아니면 읽음 처리를 하지 않는다")
    void should_not_handle_if_not_a_participant() {
        // given
        long roomId = 100L, userId = 1L;
        var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(userId));
        var inbound = new ChatInboundMessage(roomId, MessageType.READ, null);

        when(guard.ensureParticipant(session, roomId, userId)).thenReturn(false);

        // when
        handler.handle(session, inbound);

        // then
        verify(messageUseCase, never()).markMessagesAsRead(any());
    }

    @Test
    @DisplayName("읽음 처리 중 예외가 발생하면 세션을 종료한다")
    void should_close_session_on_exception() {
        // given
        long roomId = 100L, userId = 1L;
        var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(userId));
        var inbound = new ChatInboundMessage(roomId, MessageType.READ, null);

        when(guard.ensureParticipant(session, roomId, userId)).thenReturn(true);
        when(messageUseCase.markMessagesAsRead(any())).thenThrow(new RuntimeException("DB error"));

        // when
        handler.handle(session, inbound);

        // then
        verify(sessionManager).forceDisconnect(eq(session), any());
    }
}