package com.anonymouschat.anonymouschatserver.domain.repository;

import com.anonymouschat.anonymouschatserver.application.dto.UserSearchCondition;
import com.anonymouschat.anonymouschatserver.application.dto.UserSearchResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface UserRepositoryCustom {
	Slice<UserSearchResult> searchUsers(Long userId, UserSearchCondition condition, List<Long> blockedUserIds, Pageable pageable);
}
