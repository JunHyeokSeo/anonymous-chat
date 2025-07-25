package com.anonymouschat.anonymouschatserver.application.service.message;

import com.anonymouschat.anonymouschatserver.common.util.TestUtils;
import com.anonymouschat.anonymouschatserver.domain.chatroom.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.message.entity.Message;
import com.anonymouschat.anonymouschatserver.domain.message.repository.MessageRepository;
import com.anonymouschat.anonymouschatserver.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

	@InjectMocks
	private MessageService messageService;

	@Mock
	private MessageRepository messageRepository;

	private User user1;
	private User user2;
	private ChatRoom chatRoom;

	@BeforeEach
	void setUp() throws Exception {
		user1 = TestUtils.createUser(1L);
		user2 = TestUtils.createUser(2L);
		chatRoom = TestUtils.createChatRoom(100L, user1, user2);
	}

	@Nested
	@DisplayName("sendMessage 메서드는")
	class SendMessageTest {
		@Test
		@DisplayName("정상적으로 메시지를 저장한다")
		void sendMessage_success() {
			// given
			String content = "Hello";

			given(messageRepository.save(any(Message.class)))
					.willAnswer(invocation -> {
						Message message = invocation.getArgument(0);
						TestUtils.setId(message, 300L);
						return message;
					});

			// when
			Message result = messageService.sendMessage(chatRoom, user1, content);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getChatRoom()).isEqualTo(chatRoom);
			assertThat(result.getSender()).isEqualTo(user1);
			assertThat(result.getContent()).isEqualTo(content);
		}
	}

	@Nested
	@DisplayName("getMessages 메서드는")
	class GetMessagesTest {
		@Test
		@DisplayName("조건에 맞는 메시지 목록을 반환한다")
		void getMessages_success() throws Exception {
			// given
			LocalDateTime lastExitedAt = LocalDateTime.now().minusMinutes(10);
			Long lastMessageId = null;
			int limit = 10;

			List<Message> mockMessages = List.of(
					TestUtils.createMessage(1L, chatRoom, user2, "hi"),
					TestUtils.createMessage(2L, chatRoom, user1, "yo")
			);

			given(messageRepository.findMessagesAfterExitTimeWithCursor(chatRoom.getId(), lastExitedAt, lastMessageId, limit))
					.willReturn(mockMessages);

			// when
			List<Message> result = messageService.getMessages(chatRoom, lastExitedAt, lastMessageId, limit);

			// then
			assertThat(result).hasSize(2);
			assertThat(result.get(0).getChatRoom()).isEqualTo(chatRoom);
		}
	}

	@Nested
	@DisplayName("markMessagesAsRead 메서드는")
	class MarkMessagesAsReadTest {
		@Test
		@DisplayName("읽지 않은 메시지를 읽음 처리한다")
		void markMessagesAsRead_success() {
			// given
			Long chatRoomId = chatRoom.getId();
			Long userId = user1.getId();

			// when
			messageService.markMessagesAsRead(chatRoomId, userId);
		}
	}
}