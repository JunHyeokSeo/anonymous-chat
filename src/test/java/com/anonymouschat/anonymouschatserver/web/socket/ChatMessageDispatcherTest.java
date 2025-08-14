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

/**
 * {@link ChatMessageDispatcher}에 대한 단위 테스트 클래스입니다.
 * 수신된 메시지를 올바른 핸들러로 디스패치하는 기능을 검증합니다.
 */
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

    /**
     * 인바운드 메시지의 타입에 따라 적절한 {@link MessageHandler}가 호출되어
     * 메시지 처리를 위임하는지 검증합니다.
     */
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
        // chatMessageHandler와 enterMessageHandler의 handle 메소드가 각 메시지로 한 번씩 호출되었는지 확인.
        verify(chatMessageHandler).handle(session, chatMessage);
        verify(enterMessageHandler).handle(session, enterMessage);
    }

    /**
     * 디스패처에 등록되지 않은(`handlerMap`에 없는) 메시지 타입이 수신되었을 때,
     * {@link UnsupportedOperationException}이 발생하는지 검증합니다.
     */
    @Test
    @DisplayName("지원하지 않는 메시지 타입이면 UnsupportedOperationException을 던진다")
    void should_throw_exception_for_unsupported_message_type() {
        // given
        var leaveMessage = new ChatInboundMessage(1L, MessageType.LEAVE, null);

        // when & then
        // leaveMessage를 디스패치할 때 UnsupportedOperationException이 발생하고,
        // 해당 예외 메시지에 "Unsupported message type: LEAVE"가 포함되어 있는지 검증.
        assertThatThrownBy(() -> dispatcher.dispatch(session, leaveMessage))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Unsupported message type: LEAVE");
    }
}
