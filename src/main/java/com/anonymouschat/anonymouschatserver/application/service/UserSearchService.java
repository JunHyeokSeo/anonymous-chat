package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.application.dto.UserServiceDto;
import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
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

	public Slice<UserServiceDto.SearchResult> getUsersByCondition(UserServiceDto.SearchCommand command, Pageable pageable) {
		log.info("{}유저 조건 검색 - 요청자 ID={}, 성별={}, 나이범위={}~{}, 지역={}",
				LogTag.USER,
				command.id(),
				command.gender(),
				command.minAge(),
				command.maxAge(),
				command.region()
		);

		return userRepository.findUsersByCondition(command, pageable);
	}
}
