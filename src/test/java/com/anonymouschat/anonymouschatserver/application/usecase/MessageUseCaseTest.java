package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.service.ChatRoomService;
import com.anonymouschat.anonymouschatserver.application.service.MessageService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.type.ChatRoomStatus;
import com.anonymouschat.anonymouschatserver.domain.entity.Message;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MessageUseCaseTest {

	@Mock private MessageService messageService;
	@Mock private ChatRoomService chatRoomService;
	@Mock private UserService userService;
	@InjectMocks private MessageUseCase messageUseCase;

	private User user;
	private ChatRoom chatRoom;
	private Message message;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		user = User.builder()
				       .provider(OAuthProvider.GOOGLE)
				       .providerId("pid")
				       .nickname("tester")
				       .gender(Gender.MALE)
				       .age(25)
				       .region(Region.SEOUL)
				       .bio("hello")
				       .build();
		ReflectionTestUtils.setField(user, "id", 1L);

		chatRoom = ChatRoom.builder()
				           .user1(user)
				           .user2(user)
				           .build();
		ReflectionTestUtils.setField(chatRoom, "id", 1L);
		ReflectionTestUtils.setField(chatRoom, "status", ChatRoomStatus.ACTIVE);

		message = Message.builder()
				          .chatRoom(chatRoom)
				          .sender(user)
				          .content("hi")
				          .build();
		ReflectionTestUtils.setField(message, "id", 1L);
		ReflectionTestUtils.setField(message, "sentAt", LocalDateTime.now());
	}

	@Nested
	@DisplayName("sendMessage 메서드는")
	class SendMessage {

		@Test
		@DisplayName("정상적으로 메시지를 전송하고 결과를 반환한다.")
		void sendMessage() {

		}
	}

	@Nested
	@DisplayName("getMessages 메서드는")
	class GetMessages {

		@Test
		@DisplayName("해당 유저의 퇴장 이후 메시지를 페이징하여 가져온다.")
		void getMessages() {

		}
	}

	@Nested
	@DisplayName("markMessagesAsRead 메서드는")
	class MarkMessagesAsRead {

		@Test
		@DisplayName("메시지 읽음 처리만 수행한다.")
		void markMessagesAsRead() {
			MessageUseCaseDto.MarkMessagesAsRead request = new MessageUseCaseDto.MarkMessagesAsRead(1L, 1L);

			when(chatRoomService.getVerifiedChatRoomOrThrow(1L, 1L)).thenReturn(chatRoom);

			messageUseCase.markMessagesAsRead(request);

			verify(messageService).markMessagesAsRead(1L, 1L);
		}
	}
}
