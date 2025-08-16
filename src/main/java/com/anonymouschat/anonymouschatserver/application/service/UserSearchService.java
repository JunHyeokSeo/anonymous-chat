package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.application.dto.UserServiceDto;
import com.anonymouschat.anonymouschatserver.common.log.LogTag;
import com.anonymouschat.anonymouschatserver.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSearchService {
	private final UserRepository userRepository;

	public Slice<UserServiceDto.SearchResult> search(UserServiceDto.SearchCommand command, Pageable pageable) {
        log.info("{}searcherId={}, searchCondition={}", LogTag.USER, command.id(), command);
		return userRepository.searchUsers(command, pageable);
	}
}
