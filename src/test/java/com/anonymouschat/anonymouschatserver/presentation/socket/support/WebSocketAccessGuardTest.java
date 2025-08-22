package com.anonymouschat.anonymouschatserver.presentation.socket.support;

import com.anonymouschat.anonymouschatserver.application.service.ChatRoomService;
import com.anonymouschat.anonymouschatserver.presentation.socket.ChatSessionManager;
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

/**
 * {@link WebSocketAccessGuard}에 대한 단위 테스트 클래스입니다.
 * WebSocket 연결 및 메시지 처리에 대한 접근 제어(인가) 로직의 정확성을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketAccessGuard 테스트")
class WebSocketAccessGuardTest {

    @Mock
    private ChatRoomService chatRoomService;
    @Mock
    private ChatSessionManager sessionManager;

    @InjectMocks
    private WebSocketAccessGuard accessGuard;

    /**
     * 사용자가 채팅방 멤버십을 가지고 있어 입장(`ENTER`) 권한이 허용될 때,
     * `ensureEnterAllowed` 메소드가 `true`를 반환하고 세션이 종료되지 않는지 검증합니다.
     */
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

    /**
     * 사용자가 채팅방 멤버십을 가지고 있지 않아 입장(`ENTER`) 권한이 거부될 때,
     * `ensureEnterAllowed` 메소드가 `false`를 반환하고 세션이 종료되는지 검증합니다.
     */
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

    /**
     * 사용자가 채팅, 읽음 처리, 퇴장 등 특정 액션(`CHAT`/`READ`/`LEAVE`)을 수행할 때,
     * 현재 채팅방의 참여자이므로 `ensureParticipant` 메소드가 `true`를 반환하고 세션이 종료되지 않는지 검증합니다.
     */
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

    /**
     * 사용자가 채팅, 읽음 처리, 퇴장 등 특정 액션(`CHAT`/`READ`/`LEAVE`)을 수행할 때,
     * 현재 채팅방의 참여자가 아니므로 `ensureParticipant` 메소드가 `false`를 반환하고 세션이 종료되는지 검증합니다.
     */
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
