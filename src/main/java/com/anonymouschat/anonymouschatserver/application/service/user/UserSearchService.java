package com.anonymouschat.anonymouschatserver.application.service.user;

import com.anonymouschat.anonymouschatserver.application.dto.UserSearchCommand;
import com.anonymouschat.anonymouschatserver.application.dto.UserSearchResult;
import com.anonymouschat.anonymouschatserver.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSearchService {
	private final UserRepository userRepository;

	public UserSearchResult search(UserSearchCommand command) {

	}
}
