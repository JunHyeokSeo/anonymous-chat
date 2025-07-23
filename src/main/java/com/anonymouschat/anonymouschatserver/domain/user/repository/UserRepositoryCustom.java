package com.anonymouschat.anonymouschatserver.domain.user.repository;

import com.anonymouschat.anonymouschatserver.application.dto.UserSearchCondition;
import com.anonymouschat.anonymouschatserver.application.dto.UserSearchResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface UserRepositoryCustom {
	Slice<UserSearchResult> searchUsers(Long userId, UserSearchCondition condition, Pageable pageable);
}
