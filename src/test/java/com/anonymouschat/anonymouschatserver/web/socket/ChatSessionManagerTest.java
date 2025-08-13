package com.anonymouschat.anonymouschatserver.web.socket;

import com.anonymouschat.testsupport.socket.WebSocketSessionStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("ChatSessionManager 테스트")
class ChatSessionManagerTest {

    private ChatSessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new ChatSessionManager();
    }

    @Nested
    @DisplayName("세션 등록 및 교체 (registerOrReplaceSession)")
    class RegisterOrReplaceSession {

        @Test
        @DisplayName("새로운 사용자의 세션을 등록한다")
        void should_register_new_session() {
            // given
            long userId = 100L;
            var newSession = WebSocketSessionStub.open();

            // when
            sessionManager.registerOrReplaceSession(userId, newSession);

            // then
            assertThat(sessionManager.getSession(userId)).isEqualTo(newSession);
            assertThat(sessionManager.getLastActiveAt(userId)).isNotNull();
        }

        @Test
        @DisplayName("기존에 열린 세션이 있으면 닫고 새 세션으로 교체한다")
        void should_close_old_session_and_replace_with_new_one() throws IOException {
            // given
            long userId = 100L;
            var oldSession = WebSocketSessionStub.open();
            var newSession = WebSocketSessionStub.open();

            sessionManager.registerOrReplaceSession(userId, oldSession);

            // when
            sessionManager.registerOrReplaceSession(userId, newSession);

            // then
            assertThat(oldSession.isOpen()).isFalse();
            assertThat(oldSession.getCloseStatus()).isEqualTo(CloseStatus.NORMAL);
            assertThat(sessionManager.getSession(userId)).isEqualTo(newSession);
        }
    }

    @Nested
    @DisplayName("채팅방 참여 및 이탈")
    class RoomParticipation {

        @Test
        @DisplayName("사용자를 채팅방에 참여시킨다")
        void should_join_user_to_room() {
            // given
            long roomId = 200L;
            long userId = 100L;

            // when
            sessionManager.joinRoom(roomId, userId);

            // then
            assertThat(sessionManager.getParticipants(roomId)).contains(userId);
            assertThat(sessionManager.isParticipant(roomId, userId)).isTrue();
        }

        @Test
        @DisplayName("사용자를 채팅방에서 이탈시킨다")
        void should_remove_user_from_room() {
            // given
            long roomId = 200L;
            long userId = 100L;
            sessionManager.joinRoom(roomId, userId);

            // when
            boolean removed = sessionManager.leaveRoom(roomId, userId);

            // then
            assertThat(removed).isTrue();
            assertThat(sessionManager.getParticipants(roomId)).doesNotContain(userId);
            assertThat(sessionManager.isParticipant(roomId, userId)).isFalse();
        }

        @Test
        @DisplayName("마지막 참여자가 나가면 채팅방 정보를 정리한다")
        void should_cleanup_room_when_last_participant_leaves() {
            // given
            long roomId = 200L;
            long userId = 100L;
            sessionManager.joinRoom(roomId, userId);

            // when
            sessionManager.leaveRoom(roomId, userId);

            // then
            assertThat(sessionManager.getParticipants(roomId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("예외 처리")
    class ExceptionHandling {
        @Test
        @DisplayName("세션 close 시 IOException이 발생해도 unregister는 호출된다")
        void should_unregister_even_if_session_close_fails() throws IOException {
            // given
            long userId = 100L;
            var session = spy(WebSocketSessionStub.open());
            doThrow(new IOException("close failed")).when(session).close(any());

            sessionManager.registerOrReplaceSession(userId, session);

            // when
            sessionManager.forceDisconnect(userId, CloseStatus.NORMAL);

            // then
            // 세션이 맵에서 제거되었는지 확인
            assertThat(sessionManager.getSession(userId)).isNull();
        }
    }
}
