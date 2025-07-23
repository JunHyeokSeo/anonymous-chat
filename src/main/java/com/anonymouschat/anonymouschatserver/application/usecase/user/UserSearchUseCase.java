package com.anonymouschat.anonymouschatserver.application.usecase.user;

import com.anonymouschat.anonymouschatserver.application.dto.UserSearchCondition;
import com.anonymouschat.anonymouschatserver.application.dto.UserSearchResult;
import com.anonymouschat.anonymouschatserver.application.service.block.BlockService;
import com.anonymouschat.anonymouschatserver.application.service.user.UserSearchService;
import com.anonymouschat.anonymouschatserver.common.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class UserSearchUseCase {
	private final UserSearchService userSearchService;
	private final BlockService blockService;

	public Slice<UserSearchResult> search(Long userId, UserSearchCondition condition, Pageable pageable) {
		List<Long> blockedUserIds = blockService.getBlockedUserIds(userId);
		return userSearchService.search(userId, condition, blockedUserIds, pageable);
	}
}

