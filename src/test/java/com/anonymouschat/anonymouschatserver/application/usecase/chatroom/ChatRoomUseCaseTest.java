package com.anonymouschat.anonymouschatserver.application.usecase.chatroom;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomSummaryResult;
import com.anonymouschat.anonymouschatserver.application.service.ChatRoomService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.application.usecase.ChatRoomUseCase;
import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
@DisplayName("ChatRoomUseCase 테스트")
class ChatRoomUseCaseTest {

	private ChatRoomService chatRoomService;
	private UserService userService;
	private ChatRoomUseCase chatRoomUseCase;

	private User user1;
	private User user2;

	@BeforeEach
	void setUp() throws Exception {
		chatRoomService = mock(ChatRoomService.class);
		userService = mock(UserService.class);
		chatRoomUseCase = new ChatRoomUseCase(chatRoomService, userService);

		user1 = User.builder()
				        .provider(OAuthProvider.GOOGLE)
				        .providerId("u1")
				        .nickname("User1")
				        .gender(Gender.MALE)
				        .age(30)
				        .region(Region.SEOUL)
				        .bio("bio")
				        .build();

		user2 = User.builder()
				        .provider(OAuthProvider.APPLE)
				        .providerId("u2")
				        .nickname("User2")
				        .gender(Gender.FEMALE)
				        .age(28)
				        .region(Region.BUSAN)
				        .bio("bio")
				        .build();

		setId(user1, 1L);
		setId(user2, 2L);
	}

	private void setId(User user, Long id) throws Exception {
		Field idField = User.class.getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(user, id);
	}

	@Nested
	@DisplayName("createOrFind")
	class CreateOrFind {

		@Test
		@DisplayName("유저를 조회하고 채팅방 생성 또는 조회")
		void createOrFindChatRoom() {
			when(userService.findUser(1L)).thenReturn(user1);
			when(userService.findUser(2L)).thenReturn(user2);

			ChatRoom chatRoom = new ChatRoom(user1, user2);
			when(chatRoomService.createOrFind(user1, user2)).thenReturn(chatRoom);

			Long result = chatRoomUseCase.createOrFind(1L, 2L);

			assertThat(result).isEqualTo(chatRoom.getId());
		}
	}

	@Nested
	@DisplayName("getMyActiveChatRooms")
	class GetMyActiveChatRooms {

		@Test
		@DisplayName("내 채팅방 목록 반환")
		void getMyRooms() {
			ChatRoomSummaryResult summary = mock(ChatRoomSummaryResult.class);
			when(chatRoomService.getMyActiveChatRooms(1L)).thenReturn(Collections.singletonList(summary));

			List<ChatRoomSummaryResult> result = chatRoomUseCase.getMyActiveChatRooms(1L);

			assertThat(result).hasSize(1);
		}
	}

	@Nested
	@DisplayName("exitChatRoom")
	class ExitChatRoom {

		@Test
		@DisplayName("채팅방 나가기")
		void exitRoom() {
			chatRoomUseCase.exitChatRoom(1L, 100L);

			verify(chatRoomService).exit(1L, 100L);
		}
	}
}
