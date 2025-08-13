package com.anonymouschat.anonymouschatserver.web.socket.support;

import com.anonymouschat.anonymouschatserver.application.service.ChatRoomService;
import com.anonymouschat.anonymouschatserver.web.socket.ChatSessionManager;
import com.anonymouschat.testsupport.socket.WebSocketSessionStub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketAccessGuard 테스트")
class WebSocketAccessGuardTest {

    @Mock
    private ChatRoomService chatRoomService;
    @Mock
    private ChatSessionManager sessionManager;

    @InjectMocks
    private WebSocketAccessGuard accessGuard;

    @Test
    @DisplayName("ENTER 시 채팅방 멤버이면 접근을 허용한다")
    void should_allow_enter_when_user_is_a_member() {
        // given
        long roomId = 100L, userId = 1L;
        WebSocketSession session = WebSocketSessionStub.open();
        when(chatRoomService.isMember(roomId, userId)).thenReturn(true);

        // when
        boolean allowed = accessGuard.ensureEnterAllowed(session, roomId, userId);

        // then
        assertThat(allowed).isTrue();
        verify(sessionManager, never()).forceDisconnect(any(WebSocketSession.class), any());
    }

    @Test
    @DisplayName("ENTER 시 채팅방 멤버가 아니면 접근을 거부하고 세션을 종료한다")
    void should_deny_enter_when_user_is_not_a_member() {
        // given
        long roomId = 100L, userId = 1L;
        WebSocketSession session = WebSocketSessionStub.open();
        when(chatRoomService.isMember(roomId, userId)).thenReturn(false);

        // when
        boolean allowed = accessGuard.ensureEnterAllowed(session, roomId, userId);

        // then
        assertThat(allowed).isFalse();
        verify(sessionManager).forceDisconnect(eq(session), any());
    }

    @Test
    @DisplayName("CHAT/READ/LEAVE 시 참여자이면 접근을 허용한다")
    void should_allow_action_when_user_is_a_participant() {
        // given
        long roomId = 100L, userId = 1L;
        WebSocketSession session = WebSocketSessionStub.open();
        when(sessionManager.isParticipant(roomId, userId)).thenReturn(true);

        // when
        boolean allowed = accessGuard.ensureParticipant(session, roomId, userId);

        // then
        assertThat(allowed).isTrue();
        verify(sessionManager, never()).forceDisconnect(any(WebSocketSession.class), any());
    }

    @Test
    @DisplayName("CHAT/READ/LEAVE 시 참여자가 아니면 접근을 거부하고 세션을 종료한다")
    void should_deny_action_when_user_is_not_a_participant() {
        // given
        long roomId = 100L, userId = 1L;
        WebSocketSession session = WebSocketSessionStub.open();
        when(sessionManager.isParticipant(roomId, userId)).thenReturn(false);

        // when
        boolean allowed = accessGuard.ensureParticipant(session, roomId, userId);

        // then
        assertThat(allowed).isFalse();
        verify(sessionManager).forceDisconnect(eq(session), any());
    }
}
