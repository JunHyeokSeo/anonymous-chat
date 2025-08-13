package com.anonymouschat.anonymouschatserver.web.socket;

import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.web.socket.handler.MessageHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMessageDispatcher 테스트")
class ChatMessageDispatcherTest {

    @Mock private MessageHandler chatMessageHandler;
    @Mock private MessageHandler enterMessageHandler;
    @Mock private WebSocketSession session;

    private ChatMessageDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        when(chatMessageHandler.type()).thenReturn(MessageType.CHAT);
        when(enterMessageHandler.type()).thenReturn(MessageType.ENTER);
        dispatcher = new ChatMessageDispatcher(List.of(chatMessageHandler, enterMessageHandler));
    }

    @Test
    @DisplayName("메시지 타입에 맞는 핸들러를 찾아 처리를 위임한다")
    void should_dispatch_to_correct_handler_based_on_type() {
        // given
        var chatMessage = new ChatInboundMessage(1L, MessageType.CHAT, "hello");
        var enterMessage = new ChatInboundMessage(1L, MessageType.ENTER, null);

        // when
        dispatcher.dispatch(session, chatMessage);
        dispatcher.dispatch(session, enterMessage);

        // then
        verify(chatMessageHandler).handle(session, chatMessage);
        verify(enterMessageHandler).handle(session, enterMessage);
    }

    @Test
    @DisplayName("지원하지 않는 메시지 타입이면 UnsupportedOperationException을 던진다")
    void should_throw_exception_for_unsupported_message_type() {
        // given
        var leaveMessage = new ChatInboundMessage(1L, MessageType.LEAVE, null);

        // when & then
        assertThatThrownBy(() -> dispatcher.dispatch(session, leaveMessage))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Unsupported message type: LEAVE");
    }
}
