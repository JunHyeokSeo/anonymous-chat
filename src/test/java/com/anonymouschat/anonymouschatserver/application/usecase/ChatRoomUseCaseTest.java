package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomServiceDto;
import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.service.ChatRoomService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.NotFoundException;
import com.anonymouschat.anonymouschatserver.common.exception.chat.NotChatRoomMemberException;
import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.testsupport.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@DisplayName("ChatRoomUseCase 단위 테스트")
class ChatRoomUseCaseTest {

	@Mock
	private ChatRoomService chatRoomService;
	@Mock
	private UserService userService;

	@InjectMocks
	private ChatRoomUseCase chatRoomUseCase;

	private User user1;
	private User user2;
	private ChatRoom chatRoom;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
		user1 = TestUtils.createUser(1L);
		user2 = TestUtils.createUser(2L);
		chatRoom = TestUtils.createChatRoom(1L, user1, user2);
	}

	@Nested
	@DisplayName("채팅방 생성 또는 조회(createOrFind)")
	class CreateOrFind {

		@Test
		@DisplayName("정상적으로 신규 채팅방을 생성한다")
		void createSuccess() {
			given(userService.findUser(user1.getId())).willReturn(user1);
			given(userService.findUser(user2.getId())).willReturn(user2);
			given(chatRoomService.createOrFind(user1, user2)).willReturn(chatRoom);

			Long roomId = chatRoomUseCase.createOrFind(user1.getId(), user2.getId());

			assertThat(roomId).isEqualTo(chatRoom.getId());
		}

		@Test
		@DisplayName("존재하지 않는 유저일 경우 예외 발생")
		void userNotFound() {
			given(userService.findUser(999L)).willThrow(new NotFoundException(ErrorCode.USER_NOT_FOUND));

			assertThatThrownBy(() -> chatRoomUseCase.createOrFind(999L, user2.getId()))
					.isInstanceOf(NotFoundException.class)
					.hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("내 활성 채팅방 목록 조회(getMyActiveChatRooms)")
	class GetMyActiveChatRooms {

		@Test
		@DisplayName("정상적으로 활성 채팅방 목록을 조회한다")
		void success() {
			ChatRoomServiceDto.SummaryResult result = new ChatRoomServiceDto.SummaryResult(chatRoom.getId(), user2.getId(), user2.getNickname(), 10, "BUSAN", "file://Image.url", LocalDateTime.now(), "lastMessageContent", 10L);

			given(chatRoomService.getMyActiveChatRooms(user1.getId())).willReturn(List.of(result));

			List<ChatRoomUseCaseDto.SummaryResponse> responses = chatRoomUseCase.getMyActiveChatRooms(user1.getId());

			assertThat(responses).hasSize(1);
			assertThat(responses.getFirst().roomId()).isEqualTo(chatRoom.getId());
			assertThat(responses.getFirst().opponentId()).isEqualTo(user2.getId());
		}

		@Test
		@DisplayName("활성 채팅방이 없으면 빈 리스트 반환")
		void emptyList() {
			given(chatRoomService.getMyActiveChatRooms(user1.getId())).willReturn(List.of());

			List<ChatRoomUseCaseDto.SummaryResponse> responses = chatRoomUseCase.getMyActiveChatRooms(user1.getId());

			assertThat(responses).isEmpty();
		}
	}

	@Nested
	@DisplayName("채팅방 나가기(exitChatRoom)")
	class ExitChatRoom {

		@Test
		@DisplayName("정상적으로 채팅방에서 나간다")
		void success() {
			given(chatRoomService.getVerifiedChatRoomOrThrow(user1.getId(), chatRoom.getId()))
					.willReturn(chatRoom);

			willDoNothing().given(chatRoomService).exit(user1.getId(), chatRoom);

			chatRoomUseCase.exitChatRoom(user1.getId(), chatRoom.getId());

			then(chatRoomService).should().exit(user1.getId(), chatRoom);
		}

		@Test
		@DisplayName("채팅방 참여자가 아닌 경우 예외 발생")
		void notParticipant() {
			given(chatRoomService.getVerifiedChatRoomOrThrow(999L, chatRoom.getId()))
					.willThrow(new NotChatRoomMemberException(ErrorCode.NOT_CHAT_ROOM_MEMBER));

			assertThatThrownBy(() -> chatRoomUseCase.exitChatRoom(999L, chatRoom.getId()))
					.isInstanceOf(NotChatRoomMemberException.class)
					.hasMessage(ErrorCode.NOT_CHAT_ROOM_MEMBER.getMessage());
		}
	}
}
