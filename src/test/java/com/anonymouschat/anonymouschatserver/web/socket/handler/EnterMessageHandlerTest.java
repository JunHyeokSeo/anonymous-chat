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

@ExtendWith(MockitoExtension.class)
@DisplayName("EnterMessageHandler 테스트")
class EnterMessageHandlerTest {

    @Mock private ChatSessionManager sessionManager;
    @Mock private WebSocketAccessGuard guard;
    @InjectMocks private EnterMessageHandler handler;

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
        verify(sessionManager).joinRoom(roomId, userId);
    }

    @Test
    @DisplayName("채팅방 멤버가 아니면 참여 처리를 하지 않는다")
    void should_not_join_room_if_user_is_not_a_member() {
        // given
        long roomId = 100L, userId = 1L;
        var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(userId));
        var inbound = new ChatInboundMessage(roomId, MessageType.ENTER, null);

        when(guard.ensureEnterAllowed(session, roomId, userId)).thenReturn(false);

        // when
        handler.handle(session, inbound);

        // then
        verify(sessionManager, never()).joinRoom(anyLong(), anyLong());
    }
}
