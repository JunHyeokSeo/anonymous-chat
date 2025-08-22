package com.anonymouschat.anonymouschatserver.application.listener;

import com.anonymouschat.anonymouschatserver.application.event.ChatPersisted;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.ChatOutboundMessage;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.presentation.socket.support.MessageBroadcaster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatBroadcastListener 테스트")
class ChatBroadcastListenerTest {

    @Mock
    private MessageBroadcaster messageBroadcaster;

    private ChatBroadcastListener listener;

    @Captor
    private ArgumentCaptor<ChatOutboundMessage> outboundMessageCaptor;

    @BeforeEach
    void setUp() {
        listener = new ChatBroadcastListener(messageBroadcaster);
    }

    @Test
    @DisplayName("ChatPersisted 이벤트 수신 시 Outbound 메시지로 변환하여 브로드캐스트를 위임한다")
    void on_chat_persisted_event_then_delegate_to_broadcaster() {
        // given
        var event = new ChatPersisted(100L, 200L, 999L, "새로운 메시지", Instant.now());

        // when
        listener.on(event);

        // then
        verify(messageBroadcaster).broadcast(eq(100L), outboundMessageCaptor.capture());
        ChatOutboundMessage capturedMessage = outboundMessageCaptor.getValue();

        assertThat(capturedMessage.type()).isEqualTo(MessageType.CHAT);
        assertThat(capturedMessage.roomId()).isEqualTo(100L);
        assertThat(capturedMessage.senderId()).isEqualTo(200L);
        assertThat(capturedMessage.content()).isEqualTo("새로운 메시지");
    }
}