package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.domain.entity.Block;
import com.anonymouschat.anonymouschatserver.domain.repository.BlockRepository;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockService {
	private final BlockRepository blockRepository;
	private final UserRepository userRepository;

	public void block(Long blockerId, Long blockedId) {
		User blocker = findUserById(blockerId);
		User blocked = findUserById(blockedId);

		Block block = Block.builder()
				              .blocker(blocker)
				              .blocked(blocked)
				              .build();

		blockRepository.save(block);
	}

	public void unblock(Long blockerId, Long blockedId) {
		Block block = findBlockByBlockerIdAndBlockedId(blockerId, blockedId);
		block.deactivate();
	}

	public List<Long> getBlockedUserIds(Long blockerId) {
		return blockRepository.findAllBlockedUserIdsByBlockerId(blockerId);
	}

	private User findUserById(Long userId) {
		return userRepository.findById(userId)
				       .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
	}

	private Block findBlockByBlockerIdAndBlockedId(Long blockerId, Long blockedId) {
		return blockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId)
				       .orElseThrow(() -> new IllegalStateException("차단 정보를 찾을 수 없습니다."));
	}
}
