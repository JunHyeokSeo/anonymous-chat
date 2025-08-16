package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.NotFoundException;
import com.anonymouschat.anonymouschatserver.common.exception.user.CannotBlockSelfException;
import com.anonymouschat.anonymouschatserver.common.log.LogTag;
import com.anonymouschat.anonymouschatserver.domain.entity.Block;
import com.anonymouschat.anonymouschatserver.domain.repository.BlockRepository;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
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
				log.info("{}차단 재활성화 - blockerId={}, blockedId={}", LogTag.BLOCK, blocker.getId(), blocked.getId());
			} else {
				log.info("{}이미 차단된 상태 - blockerId={}, blockedId={}", LogTag.BLOCK, blocker.getId(), blocked.getId());
			}
		} else {
			Block block = Block.builder()
					              .blocker(blocker)
					              .blocked(blocked)
					              .build();
			blockRepository.save(block);
			log.info("{}새 유저 차단 등록 - blockerId={}, blockedId={}", LogTag.BLOCK, blocker.getId(), blocked.getId());
		}
	}

	@Transactional
	public void unblock(Long blockerId, Long blockedId) {
		Block block = findBlockByBlockerIdAndBlockedId(blockerId, blockedId);
		block.deactivate();
		log.info("{}유저 차단 해제 완료 - blockerId={}, blockedId={}", LogTag.BLOCK, blockerId, blockedId);
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
