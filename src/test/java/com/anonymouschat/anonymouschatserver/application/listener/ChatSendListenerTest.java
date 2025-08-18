package com.anonymouschat.anonymouschatserver.application.listener;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.event.ChatPersisted;
import com.anonymouschat.anonymouschatserver.application.event.ChatSend;
import com.anonymouschat.anonymouschatserver.application.usecase.MessageUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatSendOrchestrator 테스트")
class ChatSendListenerTest {
    @Mock private MessageUseCase messageUseCase;
    @Mock private ApplicationEventPublisher publisher;

    private ChatSendListener orchestrator;

    @Captor
    private ArgumentCaptor<MessageUseCaseDto.SendMessage> sendMessageCaptor;
    @Captor
    private ArgumentCaptor<ChatPersisted> persistedEventCaptor;

    @BeforeEach
    void setUp() {
        orchestrator = new ChatSendListener(messageUseCase, publisher);
    }

    @Test
    @DisplayName("ChatSave 이벤트 수신 시 메시지 전송을 호출하고 ChatPersisted 이벤트를 발행한다")
    void on_chat_save_event_then_send_message_and_publish_persisted_event() {
        // given
        var chatSaveEvent = new ChatSend(100L, 200L, "안녕하세요");
	    Long mockMessageId = 999L;
        when(messageUseCase.sendMessage(any(MessageUseCaseDto.SendMessage.class))).thenReturn(mockMessageId);

        // when
        orchestrator.on(chatSaveEvent);

        verify(messageUseCase).sendMessage(sendMessageCaptor.capture());
        MessageUseCaseDto.SendMessage capturedRequest = sendMessageCaptor.getValue();
        assertThat(capturedRequest.roomId()).isEqualTo(200L);
        assertThat(capturedRequest.senderId()).isEqualTo(100L);
        assertThat(capturedRequest.content()).isEqualTo("안녕하세요");

        verify(publisher).publishEvent(persistedEventCaptor.capture());
        ChatPersisted capturedEvent = persistedEventCaptor.getValue();
        assertThat(capturedEvent.roomId()).isEqualTo(200L);
        assertThat(capturedEvent.senderId()).isEqualTo(100L);
        assertThat(capturedEvent.messageId()).isEqualTo(mockMessageId);
        assertThat(capturedEvent.content()).isEqualTo("안녕하세요");
    }
}