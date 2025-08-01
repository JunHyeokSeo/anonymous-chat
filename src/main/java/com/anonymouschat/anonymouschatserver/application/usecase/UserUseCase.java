package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.*;
import com.anonymouschat.anonymouschatserver.application.service.BlockService;
import com.anonymouschat.anonymouschatserver.application.service.UserSearchService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class UserUseCase {

	private final UserService userService;
	private final UserSearchService userSearchService;
	private final BlockService blockService;

	public Long register(UserUseCaseDto.Register register, List<MultipartFile> images) throws IOException {
		return userService.register(UserServiceDto.RegisterCommand.from(register), images);
	}

	public UserUseCaseDto.Profile getMyProfile(Long userId) {
		return UserUseCaseDto.Profile.from(userService.getMyProfile(userId));
	}

	public void update(UserUseCaseDto.Update update, List<MultipartFile> images) throws IOException {
		userService.update(UserServiceDto.UpdateCommand.from(update), images);
	}

	public void withdraw(Long userId) {
		userService.withdraw(userId);
	}

	public Slice<UserUseCaseDto.SearchResult> search(UserUseCaseDto.SearchCondition search, Pageable pageable) {
		List<Long> blockedUserIds = blockService.getBlockedUserIds(search.id());
		UserServiceDto.SearchCommand command = UserServiceDto.SearchCommand.from(search, blockedUserIds);
		return userSearchService.search(command, pageable).map(UserUseCaseDto.SearchResult::from);
	}
}
