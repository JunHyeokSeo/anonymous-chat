package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.application.dto.ChatRoomServiceDto;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.ConflictException;
import com.anonymouschat.anonymouschatserver.common.exception.NotFoundException;
import com.anonymouschat.anonymouschatserver.common.exception.chat.NotChatRoomMemberException;
import com.anonymouschat.anonymouschatserver.domain.entity.ChatRoom;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.repository.ChatRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatRoomService 테스트")
class ChatRoomServiceTest {

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private User initiator;

    @Mock
    private User recipient;

    @BeforeEach
    void setUp() {
        lenient().when(initiator.getId()).thenReturn(1L);
        lenient().when(recipient.getId()).thenReturn(2L);
    }

    @Nested
    @DisplayName("createOrFind 메소드는")
    class Describe_createOrFind {

        private ChatRoom chatRoom;

        @BeforeEach
        void setUp() {
            chatRoom = spy(new ChatRoom(initiator, recipient));
        }

        @Test
        @DisplayName("활성 채팅방이 존재하면 그 채팅방을 반환한다")
        void it_returns_existing_active_chat_room() {
            // given
            when(chatRoomRepository.findActiveByPair(1L, 2L)).thenReturn(Optional.of(chatRoom));

            // when
            ChatRoom result = chatRoomService.createOrFind(initiator, recipient);

            // then
            assertThat(result).isEqualTo(chatRoom);
            verify(chatRoom).returnBy(initiator.getId());
            verify(chatRoomRepository, never()).save(any());
        }

        @Test
        @DisplayName("활성 채팅방이 없으면 새로 생성하여 반환한다")
        void it_creates_new_chat_room_if_not_exist() {
            // given
            when(chatRoomRepository.findActiveByPair(1L, 2L)).thenReturn(Optional.empty());
            when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

            // when
            ChatRoom result = chatRoomService.createOrFind(initiator, recipient);

            // then
            assertThat(result).isEqualTo(chatRoom);
            verify(chatRoomRepository).save(any(ChatRoom.class));
        }

        @Test
        @DisplayName("동시성 문제로 저장 실패 시, 다시 조회하여 반환한다")
        void it_handles_concurrency_issue() {
            // given
            when(chatRoomRepository.findActiveByPair(1L, 2L))
                    .thenReturn(Optional.empty())
                    .thenReturn(Optional.of(chatRoom));
            when(chatRoomRepository.save(any(ChatRoom.class)))
                    .thenThrow(DataIntegrityViolationException.class);

            // when
            ChatRoom result = chatRoomService.createOrFind(initiator, recipient);

            // then
            assertThat(result).isEqualTo(chatRoom);
            verify(chatRoomRepository).save(any(ChatRoom.class));
            verify(chatRoomRepository, times(2)).findActiveByPair(1L, 2L);
        }
    }

    @Nested
    @DisplayName("getVerifiedChatRoomOrThrow 메소드는")
    class Describe_getVerifiedChatRoomOrThrow {
        private ChatRoom chatRoom;

        @BeforeEach
        void setUp() {
            chatRoom = spy(new ChatRoom(initiator, recipient));
        }

        @Test
        @DisplayName("채팅방이 존재하고 사용자가 참여자이면 채팅방을 반환한다")
        void it_returns_chat_room_if_verified() {
            // given
            when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(chatRoom));

            // when
            ChatRoom result = chatRoomService.getVerifiedChatRoomOrThrow(1L, 10L);

            // then
            assertThat(result).isEqualTo(chatRoom);
            verify(chatRoom).validateParticipant(1L);
        }

        @Test
        @DisplayName("채팅방이 존재하지 않으면 예외를 던진다")
        void it_throws_exception_if_chat_room_not_found() {
            // given
            when(chatRoomRepository.findById(10L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> chatRoomService.getVerifiedChatRoomOrThrow(1L, 10L))
                    .isInstanceOf(NotFoundException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHATROOM_NOT_FOUND);
        }

        @Test
        @DisplayName("사용자가 참여자가 아니면 예외를 던진다")
        void it_throws_exception_if_user_is_not_participant() {
            // given
            when(chatRoomRepository.findById(10L)).thenReturn(Optional.of(chatRoom));
            doThrow(new NotChatRoomMemberException(ErrorCode.NOT_CHAT_ROOM_MEMBER)).when(chatRoom).validateParticipant(99L);

            // when & then
            assertThatThrownBy(() -> chatRoomService.getVerifiedChatRoomOrThrow(99L, 10L))
                    .isInstanceOf(NotChatRoomMemberException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_CHAT_ROOM_MEMBER);
        }
    }

    @Nested
    @DisplayName("getMyActiveChatRooms 메소드는")
    class Describe_getMyActiveChatRooms {
        @Test
        @DisplayName("사용자의 활성 채팅방 목록을 반환한다")
        void it_returns_active_chat_room_list() {
            // given
            List<ChatRoomServiceDto.SummaryResult> summaries = List.of(new ChatRoomServiceDto.SummaryResult(1L, 2L, "nickname", 20, "region", "url", LocalDateTime.now()));
            when(chatRoomRepository.findActiveChatRoomsByUser(1L)).thenReturn(summaries);

            // when
            List<ChatRoomServiceDto.SummaryResult> result = chatRoomService.getMyActiveChatRooms(1L);

            // then
            assertThat(result).isEqualTo(summaries);
        }
    }

    @Nested
    @DisplayName("exit 메소드는")
    class Describe_exit {
        private ChatRoom chatRoom;

        @BeforeEach
        void setUp() {
            chatRoom = spy(new ChatRoom(initiator, recipient));
        }

        @Test
        @DisplayName("채팅방에서 나간다")
        void it_exits_from_chat_room() {
            // when
            chatRoomService.exit(1L, chatRoom);

            // then
            verify(chatRoom).exitBy(1L);
        }
    }

    @Nested
    @DisplayName("markActiveIfInactive 메소드는")
    class Describe_markActiveIfInactive {
        private ChatRoom chatRoom;

        @BeforeEach
        void setUp() {
            chatRoom = spy(new ChatRoom(initiator, recipient));
        }

        @Test
        @DisplayName("채팅방이 비활성이면 활성으로 변경한다")
        void it_activates_if_inactive() {
            // given
            when(chatRoom.isActive()).thenReturn(false);

            // when
            chatRoomService.markActiveIfInactive(chatRoom);

            // then
            verify(chatRoom).activate();
        }

        @Test
        @DisplayName("채팅방이 활성이면 아무것도 하지 않는다")
        void it_does_nothing_if_active() {
            // given
            when(chatRoom.isActive()).thenReturn(true);

            // when
            chatRoomService.markActiveIfInactive(chatRoom);

            // then
            verify(chatRoom, never()).activate();
        }
    }

    @Nested
    @DisplayName("isMember 메소드는")
    class Describe_isMember {
        @Test
        @DisplayName("사용자가 멤버이면 true를 반환한다")
        void it_returns_true_if_user_is_member() {
            // given
            when(chatRoomRepository.existsByIdAndParticipantId(10L, 1L)).thenReturn(true);

            // when
            boolean result = chatRoomService.isMember(10L, 1L);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("사용자가 멤버가 아니면 false를 반환한다")
        void it_returns_false_if_user_is_not_member() {
            // given
            when(chatRoomRepository.existsByIdAndParticipantId(10L, 1L)).thenReturn(false);

            // when
            boolean result = chatRoomService.isMember(10L, 1L);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("returnBy 메소드는")
    class Describe_returnBy {
        private ChatRoom chatRoom;

        @BeforeEach
        void setUp() {
            chatRoom = spy(new ChatRoom(initiator, recipient));
        }

        @Test
        @DisplayName("사용자가 채팅방에 돌아오면 returnBy를 호출한다")
        void it_calls_returnBy() {
            // given
            when(chatRoom.isArchived()).thenReturn(false);

            // when
            chatRoomService.returnBy(chatRoom, 1L);

            // then
            verify(chatRoom).validateParticipant(1L);
            verify(chatRoom).returnBy(1L);
        }

        @Test
        @DisplayName("채팅방이 종료된 상태면 예외를 던진다")
        void it_throws_exception_if_archived() {
            // given
            when(chatRoom.isArchived()).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> chatRoomService.returnBy(chatRoom, 1L))
                    .isInstanceOf(ConflictException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHAT_ROOM_CLOSED);
        }
    }
}
