package com.anonymouschat.anonymouschatserver.application.usecase.block;

import com.anonymouschat.anonymouschatserver.application.service.block.BlockService;
import com.anonymouschat.anonymouschatserver.common.annotation.UseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class BlockUseCase {

	private final BlockService blockService;

	/**
	 * 유저를 차단합니다.
	 */
	public void blockUser(Long blockerId, Long blockedId) {
		blockService.block(blockerId, blockedId);
	}

	/**
	 * 유저 차단을 해제합니다.
	 */
	public void unblockUser(Long blockerId, Long blockedId) {
		blockService.unblock(blockerId, blockedId);
	}
}

