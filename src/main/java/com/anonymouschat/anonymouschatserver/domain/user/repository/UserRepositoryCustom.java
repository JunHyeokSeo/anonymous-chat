package com.anonymouschat.anonymouschatserver.domain.user.repository;

import com.anonymouschat.anonymouschatserver.application.dto.UserSearchCondition;
import com.anonymouschat.anonymouschatserver.application.dto.UserSearchResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
	Page<UserSearchResult> searchUsers(UserSearchCondition condition, Pageable pageable);
}
