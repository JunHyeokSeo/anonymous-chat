package com.anonymouschat.anonymouschatserver.web.socket.support;

import com.anonymouschat.anonymouschatserver.web.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatOutboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import com.anonymouschat.testsupport.socket.WebSocketSessionStub;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageBroadcaster 테스트")
class MessageBroadcasterTest {

    @Mock
    private ChatSessionManager sessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private MessageBroadcaster messageBroadcaster;

    @BeforeEach
    void setUp() {
        messageBroadcaster = new MessageBroadcaster(sessionManager, objectMapper);
    }

    @Test
    @DisplayName("채팅방의 모든 참여자에게 메시지를 브로드캐스트한다")
    void should_broadcast_to_all_participants_in_a_room() {
        // given
        long roomId = 100L;
        var participant1Session = WebSocketSessionStub.open();
        var participant2Session = WebSocketSessionStub.open();
        var message = createDummyOutboundMessage(roomId);

        when(sessionManager.getParticipants(roomId)).thenReturn(Set.of(1L, 2L));
        when(sessionManager.getSession(1L)).thenReturn(participant1Session);
        when(sessionManager.getSession(2L)).thenReturn(participant2Session);

        // when
        int deliveredCount = messageBroadcaster.broadcast(roomId, message);

        // then
        assertThat(deliveredCount).isEqualTo(2);
        assertThat(participant1Session.getSentTextPayloads()).hasSize(1);
        assertThat(participant2Session.getSentTextPayloads()).hasSize(1);
    }

    @Test
    @DisplayName("특정 사용자를 제외하고 브로드캐스트한다")
    void should_broadcast_to_all_except_excluded_user() {
        // given
        long roomId = 100L;
        long excludedUserId = 2L;
        var participant1Session = WebSocketSessionStub.open();

        var message = createDummyOutboundMessage(roomId);

        when(sessionManager.getParticipants(roomId)).thenReturn(Set.of(1L, excludedUserId));
        when(sessionManager.getSession(1L)).thenReturn(participant1Session);

        // when
        int deliveredCount = messageBroadcaster.broadcastExcept(roomId, message, excludedUserId);

        // then
        assertThat(deliveredCount).isEqualTo(1);
        assertThat(participant1Session.getSentTextPayloads()).hasSize(1);
    }

    @Test
    @DisplayName("세션이 닫혀있거나 없으면 메시지를 보내지 않고 연결을 정리한다")
    void should_not_send_to_closed_or_null_session_and_disconnect() throws IOException {
        // given
        long roomId = 100L;
        var openSession = WebSocketSessionStub.open();
        var closedSession = WebSocketSessionStub.open();
        closedSession.close();

        var message = createDummyOutboundMessage(roomId);

        // 1L: 정상, 2L: 닫힘, 3L: 세션 없음
        when(sessionManager.getParticipants(roomId)).thenReturn(Set.of(1L, 2L, 3L));
        when(sessionManager.getSession(1L)).thenReturn(openSession);
        when(sessionManager.getSession(2L)).thenReturn(closedSession);
        when(sessionManager.getSession(3L)).thenReturn(null);

        // when
        int deliveredCount = messageBroadcaster.broadcast(roomId, message);

        // then
        assertThat(deliveredCount).isEqualTo(1);
        assertThat(openSession.getSentTextPayloads()).hasSize(1);

        // 닫혔거나 없는 세션에 대해서는 강제 연결 종료를 시도해야 한다
        verify(sessionManager).forceDisconnect(eq(2L), any());
        verify(sessionManager).forceDisconnect(eq(3L), any());
    }

    @Test
    @DisplayName("메시지 직렬화 실패 시 예외를 로깅하고 0을 반환한다")
    void should_return_zero_on_serialization_failure() throws Exception {
        // given
        long roomId = 100L;
        var message = createDummyOutboundMessage(roomId);
        ObjectMapper failingMapper = spy(new ObjectMapper());
        when(failingMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("...") {});
        messageBroadcaster = new MessageBroadcaster(sessionManager, failingMapper);

        // when
        int deliveredCount = messageBroadcaster.broadcast(roomId, message);

        // then
        assertThat(deliveredCount).isEqualTo(0);
    }

    @Test
    @DisplayName("메시지 전송 중 IOException 발생 시 해당 세션을 종료하고 전송 실패로 처리한다")
    void should_disconnect_and_fail_delivery_on_io_exception_during_send() throws Exception {
        // given
        long roomId = 100L;
        WebSocketSession participantSession = mock(WebSocketSession.class);
        when(participantSession.isOpen()).thenReturn(true);
        var message = createDummyOutboundMessage(roomId);

        when(sessionManager.getParticipants(roomId)).thenReturn(Set.of(1L));
        when(sessionManager.getSession(1L)).thenReturn(participantSession);
        doThrow(new IOException("Network error")).when(participantSession).sendMessage(any());

        // when
        int deliveredCount = messageBroadcaster.broadcast(roomId, message);

        // then
        assertThat(deliveredCount).isEqualTo(0);
        verify(sessionManager).forceDisconnect(eq(1L), any());
    }

    private ChatOutboundMessage createDummyOutboundMessage(Long roomId) {
        return ChatOutboundMessage.builder()
                .type(MessageType.CHAT)
                .roomId(roomId)
                .senderId(1L)
                .content("테스트 메시지")
                .timestamp(Instant.now())
                .build();
    }
}
