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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link MessageBroadcaster} 단위 테스트.
 */
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
		var s1 = WebSocketSessionStub.open();
		var s2 = WebSocketSessionStub.open();
		var message = createDummyOutboundMessage(roomId);

		when(sessionManager.getParticipants(roomId)).thenReturn(Set.of(1L, 2L));
		when(sessionManager.getSession(1L)).thenReturn(s1);
		when(sessionManager.getSession(2L)).thenReturn(s2);

		// when
		int deliveredCount = messageBroadcaster.broadcast(roomId, message);

		// then
		assertThat(deliveredCount).isEqualTo(2);
		assertThat(s1.getSentTextPayloads()).hasSize(1);
		assertThat(s2.getSentTextPayloads()).hasSize(1);
	}

	@Test
	@DisplayName("특정 사용자를 제외하고 브로드캐스트한다")
	void should_broadcast_to_all_except_excluded_user() {
		// given
		long roomId = 100L;
		long excludedUserId = 2L;
		var s1 = WebSocketSessionStub.open();
		var message = createDummyOutboundMessage(roomId);

		when(sessionManager.getParticipants(roomId)).thenReturn(Set.of(1L, excludedUserId));
		when(sessionManager.getSession(1L)).thenReturn(s1);

		// when
		int deliveredCount = messageBroadcaster.broadcastExcept(roomId, message, excludedUserId);

		// then
		assertThat(deliveredCount).isEqualTo(1);
		assertThat(s1.getSentTextPayloads()).hasSize(1);
		verify(sessionManager, never()).getSession(excludedUserId);
	}


	@Test
	@DisplayName("세션이 닫혀있거나 없으면 메시지를 보내지 않고 연결을 정리한다")
	void should_not_send_to_closed_or_null_session_and_disconnect() throws IOException {
		// given
		long roomId = 100L;
		var open = WebSocketSessionStub.open();
		var closed = WebSocketSessionStub.open();
		closed.close();

		var message = createDummyOutboundMessage(roomId);

		when(sessionManager.getParticipants(roomId)).thenReturn(Set.of(1L, 2L, 3L));
		when(sessionManager.getSession(1L)).thenReturn(open);
		when(sessionManager.getSession(2L)).thenReturn(closed);
		when(sessionManager.getSession(3L)).thenReturn(null);

		// when
		int deliveredCount = messageBroadcaster.broadcast(roomId, message);

		// then
		assertThat(deliveredCount).isEqualTo(1);
		assertThat(open.getSentTextPayloads()).hasSize(1);
		verify(sessionManager).forceDisconnect(eq(2L), any());
		verify(sessionManager).forceDisconnect(eq(3L), any());
	}

	@Test
	@DisplayName("메시지 직렬화 실패 시 예외를 로깅하고 0을 반환한다(세션 상호작용 없음)")
	void should_return_zero_on_serialization_failure() {
		// given
		long roomId = 100L;
		var message = createDummyOutboundMessage(roomId);

		ObjectMapper failingMapper = mock(ObjectMapper.class);
		// writeValueAsString 메소드가 JsonProcessingException 예외를 발생하므로 작성해야 하는 보일러플레이트
		try {
			when(failingMapper.writeValueAsString(same(message)))
					.thenThrow(new JsonProcessingException("직렬화 실패") {});
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		messageBroadcaster = new MessageBroadcaster(sessionManager, failingMapper);

		// when
		int deliveredCount = messageBroadcaster.broadcast(roomId, message);

		// then
		assertThat(deliveredCount).isEqualTo(0);
		// 직렬화 시도는 해당 message로 한 번 실행됨
		try {
			verify(failingMapper).writeValueAsString(same(message));
		} catch (JsonProcessingException ignored) {}
		// 직렬화 단계에서 실패했으므로 세션 관련 상호작용 없음
		verify(sessionManager, never()).getParticipants(anyLong());
		verify(sessionManager, never()).getSession(anyLong());
		verify(sessionManager, never()).forceDisconnect(anyLong(), any());
	}

	@Test
	@DisplayName("메시지 전송 중 IOException 발생 시 해당 세션을 종료하고 전송 실패로 처리한다")
	void should_disconnect_and_fail_delivery_on_io_exception_during_send() throws Exception {
		// given
		long roomId = 100L;
		long userId = 1L;
		WebSocketSession session = mock(WebSocketSession.class);
		when(session.isOpen()).thenReturn(true);

		var message = createDummyOutboundMessage(roomId);

		when(sessionManager.getParticipants(roomId)).thenReturn(Set.of(userId));
		when(sessionManager.getSession(userId)).thenReturn(session);
		doThrow(new IOException("Network error")).when(session).sendMessage(any());

		// when
		int deliveredCount = messageBroadcaster.broadcast(roomId, message);

		// then
		assertThat(deliveredCount).isEqualTo(0);
		verify(sessionManager).forceDisconnect(eq(userId), any());
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
