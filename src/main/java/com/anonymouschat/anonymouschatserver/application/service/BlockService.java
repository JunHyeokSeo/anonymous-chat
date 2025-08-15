package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.NotFoundException;
import com.anonymouschat.anonymouschatserver.common.exception.user.CannotBlockSelfException;
import com.anonymouschat.anonymouschatserver.domain.entity.Block;
import com.anonymouschat.anonymouschatserver.domain.repository.BlockRepository;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockService {
    private final BlockRepository blockRepository;

    @Transactional
    public void block(User blocker, User blocked) {
        if (Objects.equals(blocker.getId(), blocked.getId())) {
            throw new CannotBlockSelfException(ErrorCode.CANNOT_BLOCK_SELF);
        }

        Optional<Block> existingBlock = blockRepository.findByBlockerIdAndBlockedId(blocker.getId(), blocked.getId());

        if (existingBlock.isPresent()) {
            Block block = existingBlock.get();
            if (!block.isActive()) {
                block.reactivate();
            }
        } else {
            Block block = Block.builder()
                    .blocker(blocker)
                    .blocked(blocked)
                    .build();
            blockRepository.save(block);
        }
    }

    @Transactional
    public void unblock(Long blockerId, Long blockedId) {
        Block block = findBlockByBlockerIdAndBlockedId(blockerId, blockedId);
        block.deactivate();
    }

    public List<Long> getBlockedUserIds(Long blockerId) {
        return blockRepository.findAllBlockedUserIdsByBlockerId(blockerId);
    }

    private Block findBlockByBlockerIdAndBlockedId(Long blockerId, Long blockedId) {
        return blockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId)
                .filter(Block::isActive)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BLOCK_NOT_FOUND));
    }
}