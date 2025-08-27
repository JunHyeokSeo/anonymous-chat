package com.anonymouschat.anonymouschatserver.presentation.socket;

import com.anonymouschat.testsupport.socket.WebSocketSessionStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * {@link ChatSessionManager}에 대한 단위 테스트 클래스입니다.
 * WebSocket 세션 관리, 채팅방 참여자 관리, 세션 활동 시간 관리 등
 * ChatSessionManager의 핵심 기능을 검증합니다.
 */
@DisplayName("ChatSessionManager 테스트")
class ChatSessionManagerTest {

    private ChatSessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new ChatSessionManager();
    }

    /**
     * 세션 등록 및 교체(`registerOrReplaceSession`) 관련 테스트
     */
    @Nested
    @DisplayName("세션 등록 및 교체 (registerOrReplaceSession)")
    class RegisterOrReplaceSession {
        /**
         * 새로운 사용자의 WebSocket 세션이 {@link ChatSessionManager}에 올바르게 등록되는지 검증합니다.
         */
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

        /**
         * 이미 열려있는 기존 세션이 있을 때, 해당 세션이 정상적으로 종료(`CloseStatus.NORMAL`)되고
         * 새로운 세션으로 교체되는지 검증합니다.
         */
        @Test
        @DisplayName("기존에 열린 세션이 있으면 닫고 새 세션으로 교체한다")
        void should_close_old_session_and_replace_with_new_one() {
            // given
            long userId = 100L;
            // 기존 세션과 새 세션 스텁을 생성.
            var oldSession = WebSocketSessionStub.open();
            var newSession = WebSocketSessionStub.open();

            // 기존 세션을 먼저 등록.
            sessionManager.registerOrReplaceSession(userId, oldSession);

            // when
            // 새 세션으로 교체 등록.
            sessionManager.registerOrReplaceSession(userId, newSession);

            // then
            // 기존 세션이 닫혔는지 검증.
            assertThat(oldSession.isOpen()).isFalse();
            // 기존 세션의 종료 상태가 NORMAL 인지 검증.
            assertThat(oldSession.getCloseStatus()).isEqualTo(CloseStatus.NORMAL);
            // 현재 세션이 새 세션으로 교체되었는지 검증.
            assertThat(sessionManager.getSession(userId)).isEqualTo(newSession);
        }
    }

    /**
     * 채팅방 참여 및 이탈 관련 테스트
     */
    @Nested
    @DisplayName("채팅방 참여 및 이탈")
    class RoomParticipation {

        /**
         * 사용자가 특정 채팅방에 올바르게 참여(`joinRoom`)하는지 검증합니다.
         */
        @Test
        @DisplayName("사용자를 채팅방에 참여시킨다")
        void should_join_user_to_room() {
            // given
            long roomId = 200L;
            long userId = 100L;

            // when
            sessionManager.joinRoom(roomId, userId);

            // then
            // 해당 채팅방의 참여자 목록에 사용자가 포함되어 있는지 검증.
            assertThat(sessionManager.getParticipants(roomId)).contains(userId);
            // isParticipant 메소드가 true를 반환하는지 검증.
            assertThat(sessionManager.isParticipant(roomId, userId)).isTrue();
        }

        /**
         * 사용자가 특정 채팅방에서 올바르게 이탈(`leaveRoom`)하는지 검증합니다.
         */
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
            // 이탈 성공 여부가 true 인지 검증.
            assertThat(removed).isTrue();
            // 해당 채팅방의 참여자 목록에 사용자가 더 이상 포함되지 않는지 검증.
            assertThat(sessionManager.getParticipants(roomId)).doesNotContain(userId);
            // isParticipant 메소드가 false를 반환하는지 검증.
            assertThat(sessionManager.isParticipant(roomId, userId)).isFalse();
        }

        /**
         * 채팅방의 마지막 참여자가 이탈했을 때,
         * 해당 채팅방 정보가 {@link ChatSessionManager}에서 올바르게 정리되는지 검증합니다.
         */
        @Test
        @DisplayName("마지막 참여자가 나가면 채팅방 정보를 정리한다")
        void should_cleanup_room_when_last_participant_leaves() {
            // given
            long roomId = 200L;
            long userId = 100L;
            sessionManager.joinRoom(roomId, userId);

            // when
	        // 마지막 참여자 나감
            sessionManager.leaveRoom(roomId, userId);

            // then
            // 해당 채팅방의 참여자 목록이 비어있는지 (정리되었는지) 검증.
            assertThat(sessionManager.getParticipants(roomId)).isEmpty();
        }
    }

    /**
     * 예외 처리 관련 테스트
     */
    @Nested
    @DisplayName("예외 처리")
    class ExceptionHandling {
        /**
         * 세션 종료(`close`) 시 `IOException`이 발생하더라도,
         * 세션 관리자에서 해당 세션 정보가 올바르게 제거(`unregisterSession`)되는지 검증합니다.
         */
        @Test
        @DisplayName("세션 close 시 IOException이 발생해도 unregister는 호출된다")
        void should_unregister_even_if_session_close_fails() {
            // given
            long userId = 100L;
            // IOException을 발생시키도록 스파이된 WebSocket 세션 스텁을 생성.
            var session = spy(WebSocketSessionStub.open());
            doThrow(new IOException("close failed")).when(session).close(any());

            sessionManager.registerOrReplaceSession(userId, session);

            // when
            sessionManager.forceDisconnect(userId, CloseStatus.NORMAL);

            // then
            // 세션이 맵에서 제거되었는지 (null 인지) 확인.
            assertThat(sessionManager.getSession(userId)).isNull();
        }
    }
}
