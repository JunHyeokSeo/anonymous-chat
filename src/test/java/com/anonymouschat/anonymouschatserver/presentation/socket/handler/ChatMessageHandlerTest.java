package com.anonymouschat.anonymouschatserver.presentation.socket.handler;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.event.MessageStoreFailure;
import com.anonymouschat.anonymouschatserver.application.event.PushNotificationRequired;
import com.anonymouschat.anonymouschatserver.application.usecase.MessageUseCase;
import com.anonymouschat.anonymouschatserver.presentation.socket.ChatSessionManager;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.ChatInboundMessage;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.ChatOutboundMessage;
import com.anonymouschat.anonymouschatserver.presentation.socket.dto.MessageType;
import com.anonymouschat.anonymouschatserver.presentation.socket.support.MessageBroadcaster;
import com.anonymouschat.anonymouschatserver.presentation.socket.support.WebSocketAccessGuard;
import com.anonymouschat.testsupport.security.PrincipalStub;
import com.anonymouschat.testsupport.socket.WebSocketSessionStub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.socket.CloseStatus;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link ChatMessageHandler} 단위 테스트 (정리 버전).
 * - 브로드캐스트
 * - 메시지 저장/실패
 * - 푸시 알림 이벤트
 * - 세션 종료 시나리오
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMessageHandler 테스트")
class ChatMessageHandlerTest {

	@Mock private ChatSessionManager sessionManager;
	@Mock private MessageBroadcaster broadcaster;
	@Mock private WebSocketAccessGuard guard;
	@Mock private MessageUseCase messageUseCase;
	@Mock private ApplicationEventPublisher publisher;
	@InjectMocks private ChatMessageHandler handler;

	@Captor private ArgumentCaptor<ChatOutboundMessage> outboundMessageCaptor;
	@Captor private ArgumentCaptor<MessageUseCaseDto.SendMessageRequest> sendMessageRequestCaptor;
	@Captor private ArgumentCaptor<PushNotificationRequired> pushNotificationCaptor;
	@Captor private ArgumentCaptor<MessageStoreFailure> messageStoreFailureCaptor;

	@Test
	@DisplayName("핸들러가 CHAT 타입을 반환한다")
	void should_return_chat_message_type() {
		assertThat(handler.type()).isEqualTo(MessageType.CHAT);
	}

	@Test
	@DisplayName("참여자가 보낸 메시지를 브로드캐스트하고 메시지를 저장한다")
	void should_broadcast_and_save_message() {
		// given
		long roomId = 100L, senderId = 1L;
		String content = "안녕하세요";
		var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(senderId));
		var inbound = new ChatInboundMessage(roomId, MessageType.CHAT, content);

		when(guard.ensureParticipant(session, roomId, senderId)).thenReturn(true);
		when(broadcaster.broadcastExcept(eq(roomId), any(ChatOutboundMessage.class), eq(senderId))).thenReturn(1);
		when(messageUseCase.sendMessage(any())).thenReturn(1000L);

		// when
		handler.handle(session, inbound);

		// then
		// 브로드캐스트 메시지 검증
		verify(broadcaster).broadcastExcept(eq(roomId), outboundMessageCaptor.capture(), eq(senderId));
		ChatOutboundMessage outbound = outboundMessageCaptor.getValue();
		assertThat(outbound.roomId()).isEqualTo(roomId);
		assertThat(outbound.senderId()).isEqualTo(senderId);
		assertThat(outbound.content()).isEqualTo(content);
		assertThat(outbound.type()).isEqualTo(MessageType.CHAT);
		assertThat(outbound.timestamp()).isNotNull();

		// 저장 요청 검증
		verify(messageUseCase).sendMessage(sendMessageRequestCaptor.capture());
		MessageUseCaseDto.SendMessageRequest request = sendMessageRequestCaptor.getValue();
		assertThat(request.roomId()).isEqualTo(roomId);
		assertThat(request.senderId()).isEqualTo(senderId);
		assertThat(request.content()).isEqualTo(content);
	}

	@Test
	@DisplayName("메시지 저장 성공 시, 온라인 수신자가 없을 때만 푸시 알림 이벤트 발행")
	void should_publish_push_notification_only_when_no_online_receivers() {
		long roomId = 100L, senderId = 1L;
		String content = "안녕하세요";
		Long messageId = 1000L;
		when(messageUseCase.sendMessage(any())).thenReturn(messageId);

		// case1: 온라인 수신자 있음
		handler.saveMessageAsync(roomId, senderId, content, false);
		verify(publisher, never()).publishEvent(any(PushNotificationRequired.class));

		// case2: 온라인 수신자 없음
		reset(publisher);
		handler.saveMessageAsync(roomId, senderId, content, true);
		verify(publisher).publishEvent(pushNotificationCaptor.capture());
		PushNotificationRequired event = pushNotificationCaptor.getValue();
		assertThat(event.roomId()).isEqualTo(roomId);
		assertThat(event.senderId()).isEqualTo(senderId);
		assertThat(event.messageId()).isEqualTo(messageId);
		assertThat(event.content()).isEqualTo(content);
	}

	@Test
	@DisplayName("메시지 저장 실패 시 실패 이벤트를 발행한다")
	void should_publish_failure_event_when_message_save_fails() {
		long roomId = 100L, senderId = 1L;
		String content = "실패 케이스";
		when(messageUseCase.sendMessage(any())).thenThrow(new RuntimeException("DB 오류"));

		handler.saveMessageAsync(roomId, senderId, content, false);

		verify(publisher).publishEvent(messageStoreFailureCaptor.capture());
		MessageStoreFailure failure = messageStoreFailureCaptor.getValue();
		assertThat(failure.roomId()).isEqualTo(roomId);
		assertThat(failure.senderId()).isEqualTo(senderId);
		assertThat(failure.content()).isEqualTo(content);
		assertThat(failure.retryCount()).isZero();
		assertThat(failure.failedAt()).isNotNull();
	}

	@Test
	@DisplayName("참여자가 아니면 메시지를 처리하지 않는다")
	void should_not_handle_message_if_not_participant() {
		long roomId = 100L, senderId = 1L;
		var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(senderId));
		var inbound = new ChatInboundMessage(roomId, MessageType.CHAT, "안녕하세요");

		when(guard.ensureParticipant(session, roomId, senderId)).thenReturn(false);

		handler.handle(session, inbound);

		verify(broadcaster, never()).broadcast(anyLong(), any());
		verify(messageUseCase, never()).sendMessage(any());
		verify(publisher, never()).publishEvent(any());
	}

	@Test
	@DisplayName("브로드캐스트 중 예외 발생 시 세션을 종료한다")
	void should_close_session_on_broadcast_exception() {
		long roomId = 100L, senderId = 1L;
		var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(senderId));
		var inbound = new ChatInboundMessage(roomId, MessageType.CHAT, "안녕하세요");

		when(guard.ensureParticipant(session, roomId, senderId)).thenReturn(true);
		doThrow(new RuntimeException("브로드캐스트 실패")).when(broadcaster).broadcastExcept(anyLong(), any(), eq(senderId));

		handler.handle(session, inbound);

		verify(sessionManager).forceDisconnect(eq(session), eq(CloseStatus.SERVER_ERROR));
	}

	@Test
	@DisplayName("guard 검증 중 예외 발생 시 세션을 종료한다")
	void should_close_session_on_guard_exception() {
		long roomId = 100L, senderId = 1L;
		var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(senderId));
		var inbound = new ChatInboundMessage(roomId, MessageType.CHAT, "안녕하세요");

		when(guard.ensureParticipant(session, roomId, senderId))
				.thenThrow(new RuntimeException("권한 검증 실패"));

		handler.handle(session, inbound);

		verify(sessionManager).forceDisconnect(eq(session), eq(CloseStatus.SERVER_ERROR));
		verify(broadcaster, never()).broadcast(anyLong(), any());
	}

	@Test
	@DisplayName("아웃바운드 메시지의 타임스탬프가 올바르게 설정된다")
	void should_set_correct_timestamp_in_outbound_message() {
		long roomId = 100L, senderId = 1L;
		var session = WebSocketSessionStub.withPrincipal(PrincipalStub.authenticated(senderId));
		var inbound = new ChatInboundMessage(roomId, MessageType.CHAT, "안녕하세요");

		Instant before = Instant.now();

		when(guard.ensureParticipant(session, roomId, senderId)).thenReturn(true);
		when(broadcaster.broadcastExcept(eq(roomId), any(ChatOutboundMessage.class), eq(senderId))).thenReturn(1);

		handler.handle(session, inbound);

		Instant after = Instant.now();

		verify(broadcaster).broadcastExcept(eq(roomId), outboundMessageCaptor.capture(), eq(senderId));
		ChatOutboundMessage message = outboundMessageCaptor.getValue();
		assertThat(message.timestamp())
				.isAfter(before.minusSeconds(1))
				.isBefore(after.plusSeconds(1));
	}
}
