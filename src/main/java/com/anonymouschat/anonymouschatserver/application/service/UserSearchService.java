package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.application.dto.UserServiceDto;
import com.anonymouschat.anonymouschatserver.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSearchService {
	private final UserRepository userRepository;

	public Slice<UserServiceDto.SearchResult> search(UserServiceDto.SearchCommand command, Pageable pageable) {
		return userRepository.searchUsers(command, pageable);
	}
}
