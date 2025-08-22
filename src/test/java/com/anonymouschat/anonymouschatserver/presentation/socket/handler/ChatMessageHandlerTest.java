package com.anonymouschat.anonymouschatserver.presentation.socket.handler;

import com.anonymouschat.anonymouschatserver.application.event.ChatSend;
import com.anonymouschat.anonymouschatserver.presentation.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.ChatOutboundMessage;
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
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link ChatMessageHandler}에 대한 단위 테스트 클래스입니다.
 * 채팅 메시지(`MessageType.CHAT`) 처리 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMessageHandler 테스트")
class ChatMessageHandlerTest {

    @Mock private ChatSessionManager sessionManager;
    @Mock private ApplicationEventPublisher publisher;
    @Mock private MessageBroadcaster broadcaster;
    @Mock private WebSocketAccessGuard guard;
    @InjectMocks private ChatMessageHandler handler;

    @Captor private ArgumentCaptor<ChatSend> chatSaveEventCaptor;
    @Captor private ArgumentCaptor<ChatOutboundMessage> outboundMessageCaptor;

    /**
     * 채팅방 참여자가 메시지를 보냈을 때,
     * 메시지 저장 이벤트가 발행되고, 채팅방 참여자들에게 메시지가 브로드캐스트되는지 검증합니다.
     */
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
        // publisher의 publishEvent 메소드가 ChatSave 이벤트로 호출되었는지 검증하고 캡처.
        verify(publisher).publishEvent(chatSaveEventCaptor.capture());
        ChatSend capturedEvent = chatSaveEventCaptor.getValue();
        // 캡처된 이벤트의 roomId, senderId, content가 예상과 일치하는지 검증.
        assertThat(capturedEvent.roomId()).isEqualTo(roomId);
        assertThat(capturedEvent.senderId()).isEqualTo(senderId);
        assertThat(capturedEvent.content()).isEqualTo("안녕하세요");

        // broadcaster의 broadcast 메소드가 올바른 roomId와 아웃바운드 메시지로 호출되었는지 검증.
        verify(broadcaster).broadcast(eq(roomId), outboundMessageCaptor.capture());
        ChatOutboundMessage capturedMessage = outboundMessageCaptor.getValue();
        // 캡처된 아웃바운드 메시지의 roomId, senderId, content가 예상과 일치하는지 검증.
        assertThat(capturedMessage.roomId()).isEqualTo(roomId);
        assertThat(capturedMessage.senderId()).isEqualTo(senderId);
        assertThat(capturedMessage.content()).isEqualTo("안녕하세요");
    }

    /**
     * 메시지를 보낸 사용자가 채팅방 참여자가 아닐 때,
     * 메시지 저장 이벤트 발행 및 브로드캐스트가 수행되지 않는지 검증합니다.
     */
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
        // publisher의 publishEvent 메소드가 호출되지 않았는지 검증.
        verify(publisher, never()).publishEvent(any());
        // broadcaster의 broadcast 메소드가 호출되지 않았는지 검증.
        verify(broadcaster, never()).broadcast(anyLong(), any());
    }

    /**
     * 메시지 처리 중 예외가 발생했을 때,
     * 세션이 `CloseStatus.SERVER_ERROR` 상태로 종료되는지 검증합니다.
     */
    @Test
    @DisplayName("처리 중 예외 발생 시 세션을 종료한다")
    void should_close_session_on_exception() {
        // given
        long roomId = 100L, senderId = 1L;
        var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(senderId));
        var inbound = new ChatInboundMessage(roomId, MessageType.CHAT, "안녕하세요");

        // guard가 참여자임을 허용하고, broadcaster가 예외를 발생시키도록 설정.
        when(guard.ensureParticipant(session, roomId, senderId)).thenReturn(true);
        doThrow(new RuntimeException("Broadcast failed")).when(broadcaster).broadcast(anyLong(), any());

        // when
        // 핸들러의 handle 메소드를 호출하여 채팅 메시지를 처리합니다.
        handler.handle(session, inbound);

        // then
        // sessionManager의 forceDisconnect 메소드가 SERVER_ERROR 상태로 호출되었는지 검증합니다.
        verify(sessionManager).forceDisconnect(eq(session), any());
    }
}
