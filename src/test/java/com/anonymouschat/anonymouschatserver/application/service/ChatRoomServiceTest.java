package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.repository.ChatRoomRepository;
import com.anonymouschat.anonymouschatserver.domain.type.ChatRoomStatus;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("ChatRoomService 테스트")
class ChatRoomServiceTest {

	private ChatRoomRepository chatRoomRepository;
	private ChatRoomService chatRoomService;

	private User user1;
	private User user2;

	@BeforeEach
	void setUp() throws Exception {
		chatRoomRepository = mock(ChatRoomRepository.class);
		chatRoomService = new ChatRoomService(chatRoomRepository);

		user1 = User.builder()
				        .provider(OAuthProvider.GOOGLE)
				        .providerId("user1")
				        .nickname("User1")
				        .gender(Gender.MALE)
				        .age(25)
				        .region(Region.SEOUL)
				        .bio("Hi there")
				        .build();

		user2 = User.builder()
				        .provider(OAuthProvider.GOOGLE)
				        .providerId("user2")
				        .nickname("User2")
				        .gender(Gender.FEMALE)
				        .age(24)
				        .region(Region.BUSAN)
				        .bio("Hello")
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
		@DisplayName("기존 채팅방이 존재하면 반환하고 returnBy 호출")
		void returnExistingChatRoom() {
			ChatRoom existingRoom = new ChatRoom(user1, user2);
			when(chatRoomRepository.findByParticipantsAndStatus(anyLong(), anyLong(), eq(ChatRoomStatus.ACTIVE)))
					.thenReturn(Optional.of(existingRoom));

			ChatRoom result = chatRoomService.createOrFind(user1, user2);

			assertThat(result).isEqualTo(existingRoom);
		}

		@Test
		@DisplayName("기존 채팅방이 없으면 새로 생성하여 저장")
		void createNewChatRoom() {
			when(chatRoomRepository.findByParticipantsAndStatus(anyLong(), anyLong(), eq(ChatRoomStatus.ACTIVE)))
					.thenReturn(Optional.empty());

			ChatRoom newRoom = new ChatRoom(user1, user2);
			when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(newRoom);

			ChatRoom result = chatRoomService.createOrFind(user1, user2);

			assertThat(result).isNotNull();
		}
	}

	@Nested
	@DisplayName("getChatRoomDetail")
	class GetChatRoomDetail {

		@Test
		@DisplayName("유효한 채팅방이면 반환")
		void returnChatRoomIfValid() {
			ChatRoom room = new ChatRoom(user1, user2);
			when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));

			ChatRoom result = chatRoomService.getVerifiedChatRoomOrThrow(1L, 1L);

			assertThat(result).isEqualTo(room);
		}

		@Test
		@DisplayName("참여자가 아니면 예외")
		void throwIfNotParticipant() {
			ChatRoom room = new ChatRoom(user1, user2);
			when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));

			assertThatThrownBy(() -> chatRoomService.getVerifiedChatRoomOrThrow(99L, 1L))
					.isInstanceOf(IllegalStateException.class);
		}
	}

	@Nested
	@DisplayName("exit")
	class Exit {

		@Test
		@DisplayName("채팅방 나가기")
		void exitChatRoom() {
			ChatRoom room = Mockito.spy(new ChatRoom(user1, user2));
			when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(room));

			chatRoomService.exit(1L, 1L);

			verify(room).exitBy(1L);
		}
	}
}
