package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.service.BlockService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.NotFoundException;
import com.anonymouschat.anonymouschatserver.common.exception.user.CannotBlockSelfException;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.testsupport.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@DisplayName("BlockUseCase 단위 테스트")
class BlockUseCaseTest {

	@Mock
	private BlockService blockService;
	@Mock
	private UserService userService;

	@InjectMocks
	private BlockUseCase blockUseCase;

	private User user1;
	private User user2;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
		user1 = TestUtils.createUser(1L);
		user2 = TestUtils.createUser(2L);
	}

	@Nested
	@DisplayName("유저 차단(blockUser)")
	class BlockUser {

		@Test
		@DisplayName("정상적으로 다른 유저를 차단한다")
		void success() {
			given(userService.findUser(user1.getId())).willReturn(user1);
			given(userService.findUser(user2.getId())).willReturn(user2);

			willDoNothing().given(blockService).block(user1, user2);

			blockUseCase.blockUser(user1.getId(), user2.getId());

			then(blockService).should().block(user1, user2);
		}

		@Test
		@DisplayName("존재하지 않는 유저를 차단하려 하면 예외 발생")
		void userNotFound() {
			given(userService.findUser(999L)).willThrow(new NotFoundException(ErrorCode.USER_NOT_FOUND));

			assertThatThrownBy(() -> blockUseCase.blockUser(999L, user2.getId()))
					.isInstanceOf(NotFoundException.class)
					.hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("자기 자신을 차단하려 하면 예외 발생")
		void cannotBlockSelf() {
			given(userService.findUser(user1.getId())).willReturn(user1);

			willThrow(new CannotBlockSelfException(ErrorCode.CANNOT_BLOCK_SELF))
					.given(blockService).block(user1, user1);

			assertThatThrownBy(() -> blockUseCase.blockUser(user1.getId(), user1.getId()))
					.isInstanceOf(CannotBlockSelfException.class)
					.hasMessage(ErrorCode.CANNOT_BLOCK_SELF.getMessage());
		}
	}

	@Nested
	@DisplayName("유저 차단 해제(unblockUser)")
	class UnblockUser {

		@Test
		@DisplayName("정상적으로 차단을 해제한다")
		void success() {
			willDoNothing().given(blockService).unblock(user1.getId(), user2.getId());

			blockUseCase.unblockUser(user1.getId(), user2.getId());

			then(blockService).should().unblock(user1.getId(), user2.getId());
		}

		@Test
		@DisplayName("차단 내역이 없으면 예외 발생")
		void notFound() {
			willThrow(new NotFoundException(ErrorCode.BLOCK_NOT_FOUND))
					.given(blockService).unblock(user1.getId(), user2.getId());

			assertThatThrownBy(() -> blockUseCase.unblockUser(user1.getId(), user2.getId()))
					.isInstanceOf(NotFoundException.class)
					.hasMessage(ErrorCode.BLOCK_NOT_FOUND.getMessage());
		}
	}
}
