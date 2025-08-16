package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.service.BlockService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.annotation.UseCase;
import com.anonymouschat.anonymouschatserver.common.log.LogTag;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class BlockUseCase {
	private final BlockService blockService;
	private final UserService userService;

	/**
	 * 유저를 차단합니다.
	 */
	public void blockUser(Long blockerId, Long blockedId) {
		User blocker = userService.findUser(blockerId);
		User blocked = userService.findUser(blockedId);
		blockService.block(blocker, blocked);
		log.info("{}유저 차단 완료 - 차단자 ID={}, 피차단자 ID={}", LogTag.BLOCK, blockerId, blockedId);
	}

	/**
	 * 유저 차단을 해제합니다.
	 */
	public void unblockUser(Long blockerId, Long blockedId) {
		blockService.unblock(blockerId, blockedId);
		log.info("{}유저 차단 해제 완료 - 차단자 ID={}, 피차단자 ID={}", LogTag.BLOCK, blockerId, blockedId);
	}
}
