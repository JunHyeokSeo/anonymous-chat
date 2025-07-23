package com.anonymouschat.anonymouschatserver.application.usecase.user;

import com.anonymouschat.anonymouschatserver.application.dto.*;
import com.anonymouschat.anonymouschatserver.application.service.block.BlockService;
import com.anonymouschat.anonymouschatserver.application.service.user.UserSearchService;
import com.anonymouschat.anonymouschatserver.application.service.user.UserService;
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

	// 1. 회원 등록
	public Long register(RegisterUserCommand command, List<MultipartFile> images) throws IOException {
		if (images.size() > 3) {
			throw new IllegalStateException("이미지는 최대 3장까지 업로드할 수 있습니다.");
		}
		if (userService.checkNicknameDuplicate(command.nickname())) {
			throw new IllegalStateException("동일한 닉네임을 가진 사용자가 있습니다.");
		}
		return userService.register(command, images);
	}

	// 2. 내 프로필 조회
	public GetMyProfileResult getMyProfile(Long userId) {
		return userService.getMyProfile(userId);
	}

	// 3. 회원 정보 수정
	public UpdateUserResult update(UpdateUserCommand command, List<MultipartFile> images) throws IOException {
		if (images.size() > 3) {
			throw new IllegalStateException("이미지는 최대 3장까지 업로드할 수 있습니다.");
		}
		return userService.update(command, images);
	}

	// 4. 회원 탈퇴
	public void withdraw(Long userId) {
		userService.withdraw(userId);
	}

	// 5. 유저 필터 검색
	public Slice<UserSearchResult> search(Long userId, UserSearchCondition condition, Pageable pageable) {
		List<Long> blockedUserIds = blockService.getBlockedUserIds(userId);
		return userSearchService.search(userId, condition, blockedUserIds, pageable);
	}
}
