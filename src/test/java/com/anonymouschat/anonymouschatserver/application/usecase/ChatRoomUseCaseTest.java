package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomServiceDto;
import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.service.ChatRoomService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import com.anonymouschat.testsupport.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
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

		user1 = TestUtils.createUser(1L);
		user2 = TestUtils.createUser(2L);
	}

	@Nested
	@DisplayName("createOrFind")
	class CreateOrFind {

		@Test
		@DisplayName("유저를 조회하고 채팅방 생성 또는 조회")
		void createOrFindChatRoom() throws Exception {
			// given
			ChatRoom chatRoom = TestUtils.createChatRoom(10L, user1, user2);

			when(userService.findUser(1L)).thenReturn(user1);
			when(userService.findUser(2L)).thenReturn(user2);
			when(chatRoomService.createOrFind(user1, user2)).thenReturn(chatRoom);

			// when
			Long result = chatRoomUseCase.createOrFind(1L, 2L);

			// then
			assertThat(result).isEqualTo(10L);
			verify(userService).findUser(1L);
			verify(userService).findUser(2L);
			verify(chatRoomService).createOrFind(user1, user2);
		}
	}

	@Nested
	@DisplayName("getMyActiveChatRooms")
	class GetMyActiveChatRooms {

		@Test
		@DisplayName("내 채팅방 목록 반환")
		void getMyRooms() {
			// given
			Long userId = 1L;
			List<ChatRoomServiceDto.Summary> serviceResult = List.of(
					new ChatRoomServiceDto.Summary(
							100L,
							2L,
							"상대",
							28,
							"BUSAN",
							"https://image.url",
							LocalDateTime.now()
					)
			);

			when(chatRoomService.getMyActiveChatRooms(userId)).thenReturn(serviceResult);

			// when
			List<ChatRoomUseCaseDto.Summary> result = chatRoomUseCase.getMyActiveChatRooms(userId);

			// then
			assertThat(result).hasSize(1);
			assertThat(result.getFirst().roomId()).isEqualTo(100L);
			verify(chatRoomService).getMyActiveChatRooms(userId);
		}
	}

	@Nested
	@DisplayName("exitChatRoom")
	class ExitChatRoom {

		@Test
		@DisplayName("채팅방 나가기")
		void exitRoom() {
			// given
			Long userId = 1L;
			Long roomId = 99L;

			// when
			chatRoomUseCase.exitChatRoom(userId, roomId);

			// then
			verify(chatRoomService).exit(userId, roomId);
		}
	}
}
