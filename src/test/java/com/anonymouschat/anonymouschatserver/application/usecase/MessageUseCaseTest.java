package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.service.ChatRoomService;
import com.anonymouschat.anonymouschatserver.application.service.MessageService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.NotFoundException;
import com.anonymouschat.anonymouschatserver.common.exception.user.UserNotFoundException;
import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.entity.Message;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.testsupport.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@DisplayName("MessageUseCase 단위 테스트")
class MessageUseCaseTest {

	@Mock
	private MessageService messageService;
	@Mock
	private ChatRoomService chatRoomService;
	@Mock
	private UserService userService;

	@InjectMocks
	private MessageUseCase messageUseCase;

	private User sender;
	private User opponent;
	private ChatRoom chatRoom;
	private Message message;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
		sender = TestUtils.createUser(1L);
		opponent = TestUtils.createUser(2L);
		chatRoom = TestUtils.createChatRoom(1L, sender, opponent);
		message = TestUtils.createMessage(1L, chatRoom, sender, "안녕하세요");
	}

	@Nested
	@DisplayName("메시지 전송(sendMessage)")
	class SendMessage {

		@Test
		@DisplayName("정상적으로 메시지를 전송한다")
		void success() {
			MessageUseCaseDto.SendMessageRequest request = MessageUseCaseDto.SendMessageRequest.builder()
					                                               .roomId(chatRoom.getId())
					                                               .senderId(sender.getId())
					                                               .content("안녕하세요")
					                                               .build();

			given(chatRoomService.getVerifiedChatRoomOrThrow(sender.getId(), chatRoom.getId()))
					.willReturn(chatRoom);
			given(userService.findUser(sender.getId())).willReturn(sender);
			given(messageService.saveMessage(chatRoom, sender, "안녕하세요")).willReturn(message);

			Long messageId = messageUseCase.sendMessage(request);

			assertThat(messageId).isEqualTo(1L);
		}

		@Test
		@DisplayName("존재하지 않는 채팅방이면 예외 발생")
		void chatRoomNotFound() {
			MessageUseCaseDto.SendMessageRequest request = MessageUseCaseDto.SendMessageRequest.builder()
					                                               .roomId(99L)
					                                               .senderId(sender.getId())
					                                               .content("안녕하세요")
					                                               .build();

			given(chatRoomService.getVerifiedChatRoomOrThrow(request.senderId(), request.roomId()))
					.willThrow(new NotFoundException(ErrorCode.CHATROOM_NOT_FOUND));

			assertThatThrownBy(() -> messageUseCase.sendMessage(request))
					.isInstanceOf(NotFoundException.class)
					.hasMessage(ErrorCode.CHATROOM_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("존재하지 않는 유저가 메시지를 보내면 예외 발생")
		void userNotFound() {
			MessageUseCaseDto.SendMessageRequest request = MessageUseCaseDto.SendMessageRequest.builder()
					                                               .roomId(chatRoom.getId())
					                                               .senderId(999L)
					                                               .content("안녕하세요")
					                                               .build();

			given(chatRoomService.getVerifiedChatRoomOrThrow(request.senderId(), request.roomId()))
					.willReturn(chatRoom);
			given(userService.findUser(request.senderId()))
					.willThrow(new UserNotFoundException(ErrorCode.USER_NOT_FOUND));

			assertThatThrownBy(() -> messageUseCase.sendMessage(request))
					.isInstanceOf(UserNotFoundException.class)
					.hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("메시지 목록 조회(getMessages)")
	class GetMessages {

		@Test
		@DisplayName("정상적으로 메시지 목록을 조회한다")
		void success() {
			MessageUseCaseDto.GetMessagesRequest request = MessageUseCaseDto.GetMessagesRequest.builder()
					                                               .roomId(chatRoom.getId())
					                                               .userId(sender.getId())
					                                               .lastMessageId(null)
					                                               .limit(10)
					                                               .build();

			given(chatRoomService.getVerifiedChatRoomOrThrow(sender.getId(), chatRoom.getId()))
					.willReturn(chatRoom);
			given(messageService.getMessages(any(ChatRoom.class), any(), any(), eq(10)))
					.willReturn(List.of(message));

			var responses = messageUseCase.getMessages(request);

			assertThat(responses).isNotEmpty();
			assertThat(responses).hasSize(1);
			assertThat(responses.getFirst().content()).isEqualTo("안녕하세요");
		}

		@Test
		@DisplayName("존재하지 않는 채팅방이면 예외 발생")
		void chatRoomNotFound() {
			MessageUseCaseDto.GetMessagesRequest request = MessageUseCaseDto.GetMessagesRequest.builder()
					                                               .roomId(999L)
					                                               .userId(sender.getId())
					                                               .lastMessageId(null)
					                                               .limit(10)
					                                               .build();

			given(chatRoomService.getVerifiedChatRoomOrThrow(request.userId(), request.roomId()))
					.willThrow(new NotFoundException(ErrorCode.CHATROOM_NOT_FOUND));

			assertThatThrownBy(() -> messageUseCase.getMessages(request))
					.isInstanceOf(NotFoundException.class)
					.hasMessage(ErrorCode.CHATROOM_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("메시지 읽음 처리(markMessagesAsRead)")
	class MarkMessagesAsRead {

		@Test
		@DisplayName("정상적으로 읽음 처리를 한다")
		void success() {
			MessageUseCaseDto.MarkMessagesAsReadRequest request = MessageUseCaseDto.MarkMessagesAsReadRequest.builder()
					                                                      .roomId(chatRoom.getId())
					                                                      .userId(sender.getId())
					                                                      .build();

			given(chatRoomService.getVerifiedChatRoomOrThrow(sender.getId(), chatRoom.getId()))
					.willReturn(chatRoom);
			given(messageService.markMessagesAsRead(chatRoom.getId(), sender.getId())).willReturn(5L);

			Long count = messageUseCase.markMessagesAsRead(request);

			assertThat(count).isEqualTo(5L);
		}

		@Test
		@DisplayName("존재하지 않는 채팅방이면 예외 발생")
		void chatRoomNotFound() {
			MessageUseCaseDto.MarkMessagesAsReadRequest request = MessageUseCaseDto.MarkMessagesAsReadRequest.builder()
					                                                      .roomId(999L)
					                                                      .userId(sender.getId())
					                                                      .build();

			given(chatRoomService.getVerifiedChatRoomOrThrow(request.userId(), request.roomId()))
					.willThrow(new NotFoundException(ErrorCode.CHATROOM_NOT_FOUND));

			assertThatThrownBy(() -> messageUseCase.markMessagesAsRead(request))
					.isInstanceOf(NotFoundException.class)
					.hasMessage(ErrorCode.CHATROOM_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("상대방 마지막 읽은 메시지 ID 조회(getLastReadMessageIdByOpponent)")
	class GetLastReadMessageIdByOpponent {

		@Test
		@DisplayName("정상적으로 마지막 읽은 메시지 ID를 조회한다")
		void success() {
			MessageUseCaseDto.GetLastReadMessageRequest request = MessageUseCaseDto.GetLastReadMessageRequest.builder()
					                                                      .roomId(chatRoom.getId())
					                                                      .userId(sender.getId())
					                                                      .build();

			given(chatRoomService.getVerifiedChatRoomOrThrow(sender.getId(), chatRoom.getId()))
					.willReturn(chatRoom);
			given(messageService.findLastReadMessageIdByReceiver(chatRoom.getId(), sender.getId())).willReturn(1L);

			Long lastReadId = messageUseCase.getLastReadMessageIdByOpponent(request);

			assertThat(lastReadId).isEqualTo(1L);
		}

		@Test
		@DisplayName("존재하지 않는 채팅방이면 예외 발생")
		void chatRoomNotFound() {
			MessageUseCaseDto.GetLastReadMessageRequest request = MessageUseCaseDto.GetLastReadMessageRequest.builder()
					                                                      .roomId(999L)
					                                                      .userId(sender.getId())
					                                                      .build();

			given(chatRoomService.getVerifiedChatRoomOrThrow(request.userId(), request.roomId()))
					.willThrow(new NotFoundException(ErrorCode.CHATROOM_NOT_FOUND));

			assertThatThrownBy(() -> messageUseCase.getLastReadMessageIdByOpponent(request))
					.isInstanceOf(NotFoundException.class)
					.hasMessage(ErrorCode.CHATROOM_NOT_FOUND.getMessage());
		}
	}
}
