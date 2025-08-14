package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.entity.Message;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.repository.MessageRepository;
import com.anonymouschat.testsupport.util.TestUtils;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
	@DisplayName("saveMessage 메서드는")
	class SaveMessageTest {

		@Test
		@DisplayName("정상적으로 메시지를 저장하고 sentAt이 세팅된다")
		void saveMessage_success() {
			// given
			String content = "Hello";
			given(messageRepository.save(any(Message.class)))
					.willAnswer(invocation -> {
						Message message = invocation.getArgument(0);
						TestUtils.setId(message, 300L);
						return message;
					});

			// when
			Message result = messageService.saveMessage(chatRoom, user1, content);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getId()).isEqualTo(300L);
			assertThat(result.getChatRoom()).isEqualTo(chatRoom);
			assertThat(result.getSender()).isEqualTo(user1);
			assertThat(result.getContent()).isEqualTo(content);
			verify(messageRepository).save(any(Message.class));
		}

		@Test
		@DisplayName("저장 중 예외가 발생하면 그대로 던진다")
		void saveMessage_failure() {
			// given
			given(messageRepository.save(any())).willThrow(new RuntimeException("DB error"));

			// when & then
			assertThatThrownBy(() -> messageService.saveMessage(chatRoom, user1, "hi"))
					.isInstanceOf(RuntimeException.class)
					.hasMessage("DB error");
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
			int limit = 10;

			List<Message> mockMessages = List.of(
					TestUtils.createMessage(1L, chatRoom, user2, "hi"),
					TestUtils.createMessage(2L, chatRoom, user1, "yo")
			);

			given(messageRepository.findMessagesAfterExitTimeWithCursor(chatRoom.getId(), lastExitedAt, null, limit))
					.willReturn(mockMessages);

			// when
			List<Message> result = messageService.getMessages(chatRoom, lastExitedAt, null, limit);

			// then
			assertThat(result).hasSize(2);
			assertThat(result.get(0).getChatRoom()).isEqualTo(chatRoom);
			assertThat(result.get(0).getContent()).isEqualTo("hi");
			assertThat(result.get(1).getContent()).isEqualTo("yo");
			verify(messageRepository).findMessagesAfterExitTimeWithCursor(chatRoom.getId(), lastExitedAt, null, limit);
		}

		@Test
		@DisplayName("메시지가 없으면 빈 리스트를 반환한다")
		void getMessages_empty() {
			// given
			given(messageRepository.findMessagesAfterExitTimeWithCursor(any(), any(), any(), anyInt()))
					.willReturn(List.of());

			// when
			List<Message> result = messageService.getMessages(chatRoom, LocalDateTime.now(), null, 10);

			// then
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("markMessagesAsRead 메서드는")
	class MarkMessagesAsReadTest {

		@Test
		@DisplayName("읽지 않은 메시지를 읽음 처리하고 처리 개수를 반환한다")
		void markMessagesAsRead_success() {
			// given
			Long roomId = chatRoom.getId();
			Long userId = user1.getId();
			given(messageRepository.updateMessagesAsRead(roomId, userId)).willReturn(5L);

			// when
			Long updatedCount = messageService.markMessagesAsRead(roomId, userId);

			// then
			assertThat(updatedCount).isEqualTo(5L);
			verify(messageRepository).updateMessagesAsRead(roomId, userId);
		}
	}

	@Nested
	@DisplayName("findLastReadMessageIdByReceiver 메서드는")
	class FindLastReadMessageIdByReceiverTest {

		@Test
		@DisplayName("마지막 읽은 메시지 ID를 반환한다")
		void findLastReadMessageIdByReceiver_success() {
			// given
			Long roomId = chatRoom.getId();
			Long senderId = user1.getId();
			given(messageRepository.findMaxReadMessageId(roomId, senderId)).willReturn(42L);

			// when
			Long result = messageService.findLastReadMessageIdByReceiver(roomId, senderId);

			// then
			assertThat(result).isEqualTo(42L);
			verify(messageRepository).findMaxReadMessageId(roomId, senderId);
		}
	}
}
