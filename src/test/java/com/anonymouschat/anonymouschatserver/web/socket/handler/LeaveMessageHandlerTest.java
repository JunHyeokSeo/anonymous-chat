package com.anonymouschat.anonymouschatserver.web.socket.handler;

import com.anonymouschat.anonymouschatserver.web.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.web.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.web.socket.dto.MessageType;
import com.anonymouschat.testsupport.security.PrincipalStub;
import com.anonymouschat.testsupport.socket.WebSocketSessionStub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaveMessageHandler 테스트")
class LeaveMessageHandlerTest {

    @Mock private ChatSessionManager sessionManager;
    @InjectMocks private LeaveMessageHandler handler;

    @Test
    @DisplayName("퇴장 메시지 수신 시, sessionManager 에게 이탈 처리를 위임한다")
    void should_delegate_leave_room_to_session_manager() {
        // given
        long roomId = 100L, userId = 1L;
        var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(userId));
        var inbound = new ChatInboundMessage(roomId, MessageType.LEAVE, null);

        // when
        handler.handle(session, inbound);

        // then
        verify(sessionManager).leaveRoom(roomId, userId);
    }
}
