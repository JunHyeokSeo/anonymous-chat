package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.UserServiceDto;
import com.anonymouschat.anonymouschatserver.application.dto.UserUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.service.BlockService;
import com.anonymouschat.anonymouschatserver.application.service.UserSearchService;
import com.anonymouschat.anonymouschatserver.application.service.UserService;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.BadRequestException;
import com.anonymouschat.anonymouschatserver.common.exception.NotFoundException;
import com.anonymouschat.anonymouschatserver.common.exception.user.DuplicateNicknameException;
import com.anonymouschat.anonymouschatserver.common.exception.user.UserNotFoundException;
import com.anonymouschat.testsupport.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@DisplayName("UserUseCase 단위 테스트")
class UserUseCaseTest {

	@Mock
	private UserService userService;
	@Mock
	private UserSearchService userSearchService;
	@Mock
	private BlockService blockService;

	@InjectMocks
	private UserUseCase userUseCase;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Nested
	@DisplayName("회원가입(register)")
	class Register {

		@Test
		@DisplayName("정상적으로 회원가입에 성공한다")
		void success() throws Exception {
			// given
			UserUseCaseDto.RegisterRequest request = new UserUseCaseDto.RegisterRequest(
					"닉네임", TestUtils.createUser(1L).getGender(), 25,
					TestUtils.createUser(1L).getRegion(), "소개", TestUtils.createUser(1L).getProvider(), "provider-1"
			);

			given(userService.register(any(UserServiceDto.RegisterCommand.class), anyList())).willReturn(1L);

			// when
			Long userId = userUseCase.register(request, Collections.emptyList());

			// then
			assertThat(userId).isEqualTo(1L);
			then(userService).should().register(any(UserServiceDto.RegisterCommand.class), anyList());
		}

		@Test
		@DisplayName("게스트 유저를 찾을 수 없으면 예외 발생")
		void guestNotFound() throws Exception {
			// given
			UserUseCaseDto.RegisterRequest request = new UserUseCaseDto.RegisterRequest(
					"닉네임", TestUtils.createUser(1L).getGender(), 25,
					TestUtils.createUser(1L).getRegion(), "소개", TestUtils.createUser(1L).getProvider(), "provider-1"
			);
			willThrow(new NotFoundException(ErrorCode.USER_GUEST_NOT_FOUND)).given(userService).register(any(), anyList());

			// when & then
			assertThatThrownBy(() -> userUseCase.register(request, Collections.emptyList()))
					.isInstanceOf(NotFoundException.class);
		}

		@Test
		@DisplayName("닉네임이 중복되면 예외 발생")
		void duplicateNickname() throws Exception {
			UserUseCaseDto.RegisterRequest request = new UserUseCaseDto.RegisterRequest(
					"중복닉네임", TestUtils.createUser(1L).getGender(), 25,
					TestUtils.createUser(1L).getRegion(), "소개", TestUtils.createUser(1L).getProvider(), "provider-1"
			);
			willThrow(new DuplicateNicknameException(ErrorCode.DUPLICATE_NICKNAME)).given(userService).register(any(), anyList());

			assertThatThrownBy(() -> userUseCase.register(request, Collections.emptyList()))
					.isInstanceOf(DuplicateNicknameException.class);
		}

		@Test
		@DisplayName("이미 등록된 유저라면 예외 발생")
		void alreadyRegistered() throws Exception {
			UserUseCaseDto.RegisterRequest request = new UserUseCaseDto.RegisterRequest(
					"닉네임", TestUtils.createUser(1L).getGender(), 25,
					TestUtils.createUser(1L).getRegion(), "소개", TestUtils.createUser(1L).getProvider(), "provider-1"
			);
			willThrow(new BadRequestException(ErrorCode.ALREADY_REGISTERED_USER)).given(userService).register(any(), anyList());

			assertThatThrownBy(() -> userUseCase.register(request, Collections.emptyList()))
					.isInstanceOf(BadRequestException.class);
		}
	}

	@Nested
	@DisplayName("내 프로필 조회(getMyProfile)")
	class GetMyProfile {
		@Test
		@DisplayName("정상적으로 프로필을 조회한다")
		void success() throws Exception {
			// given
			UserServiceDto.ProfileResult profileResult = UserServiceDto.ProfileResult.builder()
					                                             .id(1L)
					                                             .nickname("테스트")
					                                             .gender(TestUtils.createUser(1L).getGender())
					                                             .age(25)
					                                             .region(TestUtils.createUser(1L).getRegion())
					                                             .bio("소개")
					                                             .profileImages(Collections.emptyList())
					                                             .build();
			given(userService.getMyProfile(1L)).willReturn(profileResult);

			// when
			var response = userUseCase.getMyProfile(1L);

			// then
			assertThat(response.nickname()).isEqualTo("테스트");
		}

		@Test
		@DisplayName("존재하지 않는 유저는 예외 발생")
		void notFound() {
			given(userService.getMyProfile(anyLong())).willThrow(new UserNotFoundException(ErrorCode.USER_NOT_FOUND));

			assertThatThrownBy(() -> userUseCase.getMyProfile(99L))
					.isInstanceOf(UserNotFoundException.class);
		}
	}

	@Nested
	@DisplayName("회원 정보 수정(update)")
	class Update {
		@Test
		@DisplayName("정상적으로 회원 정보를 수정한다")
		void success() throws Exception {
			UserUseCaseDto.UpdateRequest request = new UserUseCaseDto.UpdateRequest(
					1L, "새닉네임", TestUtils.createUser(1L).getGender(),
					26, TestUtils.createUser(1L).getRegion(), "새소개"
			);

			willDoNothing().given(userService).update(any(), anyList());

			userUseCase.update(request, Collections.emptyList());

			then(userService).should().update(any(UserServiceDto.UpdateCommand.class), anyList());
		}

		@Test
		@DisplayName("닉네임 중복 시 예외 발생")
		void duplicateNickname() throws Exception {
			UserUseCaseDto.UpdateRequest request = new UserUseCaseDto.UpdateRequest(
					1L, "중복닉네임", TestUtils.createUser(1L).getGender(),
					26, TestUtils.createUser(1L).getRegion(), "새소개"
			);
			willThrow(new DuplicateNicknameException(ErrorCode.DUPLICATE_NICKNAME)).given(userService).update(any(), anyList());

			assertThatThrownBy(() -> userUseCase.update(request, Collections.emptyList()))
					.isInstanceOf(DuplicateNicknameException.class);
		}
	}

	@Nested
	@DisplayName("회원 탈퇴(withdraw)")
	class Withdraw {
		@Test
		@DisplayName("정상적으로 회원을 탈퇴 처리한다")
		void success() {
			willDoNothing().given(userService).withdraw(1L);

			userUseCase.withdraw(1L);

			then(userService).should().withdraw(1L);
		}

		@Test
		@DisplayName("존재하지 않는 유저 탈퇴 시 예외 발생")
		void notFound() {
			willThrow(new UserNotFoundException(ErrorCode.USER_NOT_FOUND)).given(userService).withdraw(anyLong());

			assertThatThrownBy(() -> userUseCase.withdraw(99L))
					.isInstanceOf(UserNotFoundException.class);
		}
	}

	@Nested
	@DisplayName("유저 목록 조회(getUserList)")
	class GetUserList {
		@Test
		@DisplayName("정상적으로 유저 목록을 조회한다")
		void success() {
			UserUseCaseDto.SearchConditionRequest condition = UserUseCaseDto.SearchConditionRequest.builder()
					                                                  .id(1L)
					                                                  .gender(TestUtils.guestUser().getGender())
					                                                  .minAge(20).maxAge(30)
					                                                  .region(null)
					                                                  .build();

			given(blockService.getBlockedUserIds(1L)).willReturn(Collections.emptyList());
			given(userSearchService.getUsersByCondition(any(), any())).willReturn(new SliceImpl<>(Collections.emptyList()));

			var result = userUseCase.getUserList(condition, PageRequest.of(0, 10));

			assertThat(result).isNotNull();
		}
	}
}
