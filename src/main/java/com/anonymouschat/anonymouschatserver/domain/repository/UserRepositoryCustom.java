package com.anonymouschat.anonymouschatserver.domain.repository;

import com.anonymouschat.anonymouschatserver.application.dto.UserServiceDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface UserRepositoryCustom {
	Slice<UserServiceDto.SearchResult> findUsersByCondition(UserServiceDto.SearchCommand command, Pageable pageable);
}
