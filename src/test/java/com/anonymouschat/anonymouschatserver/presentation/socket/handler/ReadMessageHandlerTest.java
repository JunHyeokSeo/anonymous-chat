package com.anonymouschat.anonymouschatserver.presentation.socket.handler;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.usecase.MessageUseCase;
import com.anonymouschat.anonymouschatserver.presentation.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.presentation.socket.support.MessageBroadcaster;
import com.anonymouschat.anonymouschatserver.presentation.socket.support.WebSocketAccessGuard;
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

/**
 * {@link ReadMessageHandler}에 대한 단위 테스트 클래스입니다.
 * 메시지 읽음 처리(`MessageType.READ`) 로직 및 관련 브로드캐스트 기능을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReadMessageHandler 테스트")
class ReadMessageHandlerTest {

    @Mock private ChatSessionManager sessionManager;
    @Mock private MessageUseCase messageUseCase;
    @Mock private MessageBroadcaster broadcaster;
    @Mock private WebSocketAccessGuard guard;
    @InjectMocks private ReadMessageHandler handler;

    @Captor private ArgumentCaptor<MessageUseCaseDto.MarkMessagesAsReadRequest> captor;

    /**
     * 채팅방 참여자가 읽음 메시지를 보냈을 때,
     * 메시지가 읽음 처리되고 그 결과가 다른 참여자들에게 브로드캐스트되는지 검증합니다.
     */
    @Test
    @DisplayName("참여자가 보낸 읽음 메시지를 처리하고, 그 결과를 브로드캐스트한다")
    void should_mark_as_read_and_broadcast_on_message_from_participant() {
        // given
        long roomId = 100L, userId = 1L;
        var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(userId));
        var inbound = new ChatInboundMessage(roomId, MessageType.READ, null);

        // guard가 참여자임을 허용하고, messageUseCase가 읽음 처리 후 메시지 ID를 반환하도록 설정.
        when(guard.ensureParticipant(session, roomId, userId)).thenReturn(true);
        when(messageUseCase.markMessagesAsRead(any())).thenReturn(999L);

        // when
        handler.handle(session, inbound);

        // then
        // messageUseCase의 markMessagesAsRead 메소드가 올바른 인자로 호출되었는지 검증.
        verify(messageUseCase).markMessagesAsRead(captor.capture());
        MessageUseCaseDto.MarkMessagesAsReadRequest captured = captor.getValue();
        assertThat(captured.roomId()).isEqualTo(roomId);
        assertThat(captured.userId()).isEqualTo(userId);

        // broadcaster의 broadcastExcept 메소드가 호출되어 메시지가 브로드캐스트되었는지 검증.
        verify(broadcaster).broadcastExcept(eq(roomId), any(), eq(userId));
    }

    /**
     * 읽음 처리할 메시지가 없어서 `messageUseCase.markMessagesAsRead`가 `null`을 반환할 때,
     * 브로드캐스트가 발생하지 않는지 검증합니다.
     */
    @Test
    @DisplayName("읽음 처리할 메시지가 없으면 브로드캐스트하지 않는다")
    void should_not_broadcast_if_no_message_was_read() {
        // given
        long roomId = 100L, userId = 1L;
        var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(userId));
        var inbound = new ChatInboundMessage(roomId, MessageType.READ, null);

        // guard가 참여자임을 허용하고, messageUseCase가 null을 반환하도록 설정
        when(guard.ensureParticipant(session, roomId, userId)).thenReturn(true);
        when(messageUseCase.markMessagesAsRead(any())).thenReturn(null);

        // when
        handler.handle(session, inbound);

        // then
        // broadcaster의 broadcastExcept 메소드가 호출되지 않았는지 검증.
        verify(broadcaster, never()).broadcastExcept(anyLong(), any(), anyLong());
    }

    /**
     * 메시지를 보낸 사용자가 채팅방 참여자가 아닐 때,
     * 읽음 처리가 수행되지 않고 세션이 종료되지 않는지 검증합니다.
     */
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
        // messageUseCase의 markMessagesAsRead 메소드가 호출되지 않았는지 검증.
        verify(messageUseCase, never()).markMessagesAsRead(any());
    }

    /**
     * 메시지 읽음 처리 중 예외가 발생했을 때,
     * 세션이 `CloseStatus.SERVER_ERROR` 상태로 종료되는지 검증합니다.
     */
    @Test
    @DisplayName("읽음 처리 중 예외가 발생하면 세션을 종료한다")
    void should_close_session_on_exception() {
        // given
        long roomId = 100L, userId = 1L;
        var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(userId));
        var inbound = new ChatInboundMessage(roomId, MessageType.READ, null);

        // guard가 참여자임을 허용하고, messageUseCase가 예외를 발생시키도록 설정.
        when(guard.ensureParticipant(session, roomId, userId)).thenReturn(true);
        when(messageUseCase.markMessagesAsRead(any())).thenThrow(new RuntimeException("DB error"));

        // when
        handler.handle(session, inbound);

        // then
        // sessionManager의 forceDisconnect 메소드가 SERVER_ERROR 상태로 호출되었는지 검증.
        verify(sessionManager).forceDisconnect(eq(session), any());
    }
}