package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.NotFoundException;
import com.anonymouschat.anonymouschatserver.common.exception.user.CannotBlockSelfException;
import com.anonymouschat.anonymouschatserver.domain.entity.Block;
import com.anonymouschat.anonymouschatserver.domain.repository.BlockRepository;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.testsupport.util.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BlockService 테스트")
class BlockServiceTest {

    @InjectMocks
    private BlockService blockService;

    @Mock
    private BlockRepository blockRepository;

    @Nested
    @DisplayName("block 메소드는")
    class Describe_block {
        @Test
        @DisplayName("새로운 사용자를 차단하면")
        void block_success() throws Exception {
            // given
            User blocker = TestUtils.createUser(1L);
	        User blocked = TestUtils.createUser(2L);

            when(blockRepository.findByBlockerIdAndBlockedId(1L, 2L)).thenReturn(Optional.empty());

            // when
            blockService.block(blocker, blocked);

            // then
            verify(blockRepository).save(any(Block.class));
        }

        @Test
        @DisplayName("자기 자신을 차단하면 예외를 던진다")
        void block_fail_blockSelf() throws Exception {
            // given
            User user = TestUtils.createUser(1L);

            // when & then
            assertThatThrownBy(() -> blockService.block(user, user))
                    .isInstanceOf(CannotBlockSelfException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CANNOT_BLOCK_SELF);
        }

        @Test
        @DisplayName("이미 차단했지만 비활성화 상태이면 재활성화한다")
        void block_success_alreadyExistsInactive() throws Exception {
            // given
            User blocker = TestUtils.createUser(1L);
	        User blocked = TestUtils.createUser(2L);
            Block inactiveBlock = mock(Block.class);

            when(inactiveBlock.isActive()).thenReturn(false);
            when(blockRepository.findByBlockerIdAndBlockedId(1L, 2L)).thenReturn(Optional.of(inactiveBlock));

            // when
            blockService.block(blocker, blocked);

            // then
            verify(inactiveBlock).reactivate();
            verify(blockRepository, never()).save(any(Block.class));
        }

        @Test
        @DisplayName("이미 활성화된 상태로 차단했으면 아무것도 하지 않는다")
        void block_success_alreadyExistsActive() throws Exception {
            // given
            User blocker = TestUtils.createUser(1L);
	        User blocked = TestUtils.createUser(2L);
            Block activeBlock = mock(Block.class);

            when(activeBlock.isActive()).thenReturn(true);
            when(blockRepository.findByBlockerIdAndBlockedId(1L, 2L)).thenReturn(Optional.of(activeBlock));

            // when
            blockService.block(blocker, blocked);

            // then
            verify(activeBlock, never()).reactivate();
            verify(blockRepository, never()).save(any(Block.class));
        }
    }

    @Nested
    @DisplayName("unblock 메소드는")
    class Describe_unblock {
        @Test
        @DisplayName("차단된 사용자를 해제하면")
        void unblock_success() {
            // given
            long blockerId = 1L;
            long blockedId = 2L;
            Block block = mock(Block.class);

            when(block.isActive()).thenReturn(true);
            when(blockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId)).thenReturn(Optional.of(block));

            // when
            blockService.unblock(blockerId, blockedId);

            // then
            verify(block).deactivate();
        }

        @Test
        @DisplayName("차단 정보가 없으면 예외를 던진다")
        void unblock_fail_noBlock() {
            // given
            long blockerId = 1L;
            long blockedId = 2L;
            when(blockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> blockService.unblock(blockerId, blockedId))
                    .isInstanceOf(NotFoundException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BLOCK_NOT_FOUND);
        }

        @Test
        @DisplayName("차단 정보가 있지만 비활성 상태이면 예외를 던진다")
        void unblock_fail_inactiveBlock() {
            // given
            long blockerId = 1L;
            long blockedId = 2L;
            Block inactiveBlock = mock(Block.class);

            when(inactiveBlock.isActive()).thenReturn(false);
            when(blockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId)).thenReturn(Optional.of(inactiveBlock));

            // when & then
            assertThatThrownBy(() -> blockService.unblock(blockerId, blockedId))
                    .isInstanceOf(NotFoundException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BLOCK_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getBlockedUserIds 메소드는")
    class Describe_getBlockedUserIds {
        @Test
        @DisplayName("차단한 사용자 ID 목록을 반환한다")
        void getBlockedUserIds_success() {
            // given
            long blockerId = 1L;
            List<Long> blockedIds = List.of(2L, 3L, 4L);
            when(blockRepository.findAllBlockedUserIdsByBlockerId(blockerId)).thenReturn(blockedIds);

            // when
            List<Long> result = blockService.getBlockedUserIds(blockerId);

            // then
            assertThat(result).isEqualTo(blockedIds);
        }

        @Test
        @DisplayName("차단한 사용자가 없으면 빈 목록을 반환한다")
        void getBlockedUserIds_success_empty() {
            // given
            long blockerId = 1L;
            when(blockRepository.findAllBlockedUserIdsByBlockerId(blockerId)).thenReturn(Collections.emptyList());

            // when
            List<Long> result = blockService.getBlockedUserIds(blockerId);

            // then
            assertThat(result).isEmpty();
        }
    }
}