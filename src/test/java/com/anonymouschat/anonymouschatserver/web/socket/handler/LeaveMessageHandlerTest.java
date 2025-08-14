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

/**
 * {@link LeaveMessageHandler}에 대한 단위 테스트 클래스입니다.
 * 사용자가 채팅방을 떠나는 메시지(`MessageType.LEAVE`)를 처리하는 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LeaveMessageHandler 테스트")
class LeaveMessageHandlerTest {

    @Mock private ChatSessionManager sessionManager;
    @InjectMocks private LeaveMessageHandler handler;

    /**
     * 퇴장 메시지(`MessageType.LEAVE`)를 수신했을 때,
     * {@link ChatSessionManager}의 `leaveRoom` 메소드가 올바른 `roomId`와 `userId`로 호출되는지 검증합니다.
     */
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
        // sessionManager의 leaveRoom 메소드가 정확한 인자로 호출되었는지 검증.
        verify(sessionManager).leaveRoom(roomId, userId);
    }
}
