package com.anonymouschat.anonymouschatserver.web.socket.handler;

import com.anonymouschat.anonymouschatserver.web.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.web.socket.support.WebSocketAccessGuard;
import com.anonymouschat.testsupport.security.PrincipalStub;
import com.anonymouschat.testsupport.socket.WebSocketSessionStub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * {@link EnterMessageHandler}에 대한 단위 테스트 클래스입니다.
 * 사용자가 채팅방에 입장(`MessageType.ENTER`)하는 메시지를 처리하는 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EnterMessageHandler 테스트")
class EnterMessageHandlerTest {

    @Mock private ChatSessionManager sessionManager;
    @Mock private WebSocketAccessGuard guard;
    @InjectMocks private EnterMessageHandler handler;

    /**
     * 사용자가 채팅방 멤버십을 가지고 있어 입장 권한이 허용될 때,
     * {@link ChatSessionManager}의 `joinRoom` 메소드가 올바른 `roomId`와 `userId`로 호출되는지 검증합니다.
     */
    @Test
    @DisplayName("채팅방 멤버이면 참여 처리를 한다")
    void should_join_room_if_user_is_a_member() {
        // given
        long roomId = 100L, userId = 1L;
        var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(userId));
        var inbound = new ChatInboundMessage(roomId, MessageType.ENTER, null);

        when(guard.ensureEnterAllowed(session, roomId, userId)).thenReturn(true);

        // when
        handler.handle(session, inbound);

        // then
        // sessionManager의 joinRoom 메소드가 정확한 인자로 호출되었는지 검증
        verify(sessionManager).joinRoom(roomId, userId);
    }

    /**
     * 사용자가 채팅방 멤버십을 가지고 있지 않아 입장 권한이 거부될 때,
     * {@link ChatSessionManager}의 `joinRoom` 메소드가 호출되지 않는지 검증합니다.
     */
    @Test
    @DisplayName("채팅방 멤버가 아니면 참여 처리를 하지 않는다")
    void should_not_join_room_if_user_is_not_a_member() {
        // given
        long roomId = 100L, userId = 1L;
        var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(userId));
        var inbound = new ChatInboundMessage(roomId, MessageType.ENTER, null);

        // guard가 입장을 거부.
        when(guard.ensureEnterAllowed(session, roomId, userId)).thenReturn(false);

        // when
        handler.handle(session, inbound);

        // then
        // sessionManager의 joinRoom 메소드가 호출되지 않았는지 검증.
        verify(sessionManager, never()).joinRoom(anyLong(), anyLong());
    }
}
