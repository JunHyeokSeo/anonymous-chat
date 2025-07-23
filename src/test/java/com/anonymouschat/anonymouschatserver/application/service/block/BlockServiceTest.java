package com.anonymouschat.anonymouschatserver.application.service.block;

import com.anonymouschat.anonymouschatserver.domain.block.entity.Block;
import com.anonymouschat.anonymouschatserver.domain.block.repository.BlockRepository;
import com.anonymouschat.anonymouschatserver.domain.user.entity.User;
import com.anonymouschat.anonymouschatserver.domain.user.repository.UserRepository;
import com.anonymouschat.anonymouschatserver.domain.user.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.user.type.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BlockService 테스트")
class BlockServiceTest {

	@Mock private BlockRepository blockRepository;
	@Mock private UserRepository userRepository;
	@InjectMocks private BlockService blockService;

	private User createRealUserWithId(Long id) {
		User user = new User(OAuthProvider.GOOGLE, "providerId", "nickname", Gender.MALE, 20, Region.SEOUL, "bio");
		ReflectionTestUtils.setField(user, "id", id); // private 필드 직접 주입
		return user;
	}


	@Nested
	@DisplayName("차단 기능")
	class BlockAction {

		@Test
		@DisplayName("정상적으로 유저를 차단한다")
		void block_success() {
			Long blockerId = 1L;
			Long blockedId = 2L;

			User blocker = createRealUserWithId(blockerId);
			User blocked = createRealUserWithId(blockedId);

			when(userRepository.findById(blockerId)).thenReturn(Optional.of(blocker));
			when(userRepository.findById(blockedId)).thenReturn(Optional.of(blocked));

			blockService.block(blockerId, blockedId);

			verify(blockRepository).save(any(Block.class));
		}

		@Test
		@DisplayName("차단 실패 - 차단자 없음")
		void block_fail_noBlocker() {
			when(userRepository.findById(1L)).thenReturn(Optional.empty());

			IllegalStateException ex = assertThrows(IllegalStateException.class,
					() -> blockService.block(1L, 2L));

			assertThat(ex.getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
		}

		@Test
		@DisplayName("차단 실패 - 피차단자 없음")
		void block_fail_noBlocked() {
			User blocker = createRealUserWithId(1L);
			when(userRepository.findById(1L)).thenReturn(Optional.of(blocker));
			when(userRepository.findById(2L)).thenReturn(Optional.empty());

			IllegalStateException ex = assertThrows(IllegalStateException.class,
					() -> blockService.block(1L, 2L));

			assertThat(ex.getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
		}
	}

	@Nested
	@DisplayName("차단 해제 기능")
	class UnblockAction {

		@Test
		@DisplayName("정상적으로 유저 차단을 해제한다")
		void unblock_success() {
			Long blockerId = 1L;
			Long blockedId = 2L;

			Block block = mock(Block.class);

			when(blockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId))
					.thenReturn(Optional.of(block));

			blockService.unblock(blockerId, blockedId);

			verify(block).deactivate();
		}

		@Test
		@DisplayName("차단 해제 실패 - 차단 정보 없음")
		void unblock_fail_noBlock() {
			when(blockRepository.findByBlockerIdAndBlockedId(1L, 2L))
					.thenReturn(Optional.empty());

			IllegalStateException ex = assertThrows(IllegalStateException.class,
					() -> blockService.unblock(1L, 2L));

			assertThat(ex.getMessage()).isEqualTo("차단 정보를 찾을 수 없습니다.");
		}
	}

	@Nested
	@DisplayName("차단된 유저 목록 조회")
	class GetBlockedUserIdsAction {

		@Test
		@DisplayName("차단된 유저 ID 목록을 반환한다")
		void getBlockedUserIds_success() {
			Long blockerId = 1L;
			List<Long> blockedIds = List.of(2L, 3L, 4L);

			when(blockRepository.findAllBlockedUserIdsByBlockerId(blockerId)).thenReturn(blockedIds);

			List<Long> result = blockService.getBlockedUserIds(blockerId);

			assertThat(result).containsExactly(2L, 3L, 4L);
		}
	}
}
