package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.service.BlockService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.annotation.UseCase;
import com.anonymouschat.anonymouschatserver.common.log.LogTag;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class BlockUseCase {
	private final BlockService blockService;
	private final UserService userService;

	/**
	 * 유저를 차단합니다.
	 */
	@Transactional
	public void blockUser(Long blockerId, Long blockedId) {
		User blocker = userService.findUser(blockerId);
		User blocked = userService.findUser(blockedId);
		blockService.block(blocker, blocked);
	}

	/**
	 * 유저 차단을 해제합니다.
	 */
	@Transactional
	public void unblockUser(Long blockerId, Long blockedId) {
		blockService.unblock(blockerId, blockedId);
	}
}
