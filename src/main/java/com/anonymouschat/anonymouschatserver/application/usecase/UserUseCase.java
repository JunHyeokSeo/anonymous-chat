package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.UserServiceDto;
import com.anonymouschat.anonymouschatserver.application.dto.UserUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.service.BlockService;
import com.anonymouschat.anonymouschatserver.application.service.UserSearchService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.annotation.UseCase;
import com.anonymouschat.anonymouschatserver.infra.log.LogTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class UserUseCase {

	private final UserService userService;
	private final UserSearchService userSearchService;
	private final BlockService blockService;

	@Transactional
	public Long register(UserUseCaseDto.RegisterRequest register, List<MultipartFile> images) throws IOException {
		log.info("{}회원가입 요청 - nickname={}, gender={}, age={}", LogTag.USER, register.nickname(), register.gender(), register.age());
		Long userId = userService.register(UserServiceDto.RegisterCommand.from(register), images);
		log.info("{}회원가입 완료 - userId={}", LogTag.USER, userId);
		return userId;
	}

	@Transactional(readOnly = true)
	public UserUseCaseDto.ProfileResponse getMyProfile(Long userId) {
		return UserUseCaseDto.ProfileResponse.from(userService.getMyProfile(userId));
	}

	@Transactional
	public void update(UserUseCaseDto.UpdateRequest update, List<MultipartFile> images) throws IOException {
		log.info("{}회원 정보 수정 요청 - userId={}", LogTag.USER, update.id());
		userService.update(UserServiceDto.UpdateCommand.from(update), images);
		log.info("{}회원 정보 수정 완료 - userId={}", LogTag.USER, update.id());
	}

	@Transactional
	public void withdraw(Long userId) {
		log.info("{}회원 탈퇴 요청 - userId={}", LogTag.USER, userId);
		userService.withdraw(userId);
		log.info("{}회원 탈퇴 완료 - userId={}", LogTag.USER, userId);
	}

	@Transactional(readOnly = true)
	public Slice<UserUseCaseDto.SearchResponse> getUserList(UserUseCaseDto.SearchConditionRequest search, Pageable pageable) {
		log.info("{}유저 목록 조회 요청 - searcherId={}, condition={}", LogTag.USER, search.id(), search);
		List<Long> blockedUserIds = blockService.getBlockedUserIds(search.id());
		UserServiceDto.SearchCommand command = UserServiceDto.SearchCommand.from(search, blockedUserIds);
		Slice<UserUseCaseDto.SearchResponse> result = userSearchService.getUsersByCondition(command, pageable)
				                                            .map(UserUseCaseDto.SearchResponse::from);
		log.info("{}유저 목록 조회 완료 - 반환 유저 수={}", LogTag.USER, result.getNumberOfElements());
		return result;
	}
}
