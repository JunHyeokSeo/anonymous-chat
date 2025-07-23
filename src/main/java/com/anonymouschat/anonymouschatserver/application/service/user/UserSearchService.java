package com.anonymouschat.anonymouschatserver.application.service.user;

import com.anonymouschat.anonymouschatserver.application.dto.UserSearchCondition;
import com.anonymouschat.anonymouschatserver.application.dto.UserSearchResult;
import com.anonymouschat.anonymouschatserver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSearchService {
	private final UserRepository userRepository;

	public Slice<UserSearchResult> search(Long userId, UserSearchCondition condition, List<Long> blockedUserIds, Pageable pageable) {
		return userRepository.searchUsers(userId, condition, blockedUserIds, pageable);
	}
}
