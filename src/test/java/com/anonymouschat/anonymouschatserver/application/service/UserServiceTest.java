package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.application.dto.UserServiceDto;
import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.BadRequestException;
import com.anonymouschat.anonymouschatserver.common.exception.NotFoundException;
import com.anonymouschat.anonymouschatserver.common.exception.file.UnsupportedImageFormatException;
import com.anonymouschat.anonymouschatserver.common.exception.user.DuplicateNicknameException;
import com.anonymouschat.anonymouschatserver.common.exception.user.UserNotFoundException;
import com.anonymouschat.anonymouschatserver.common.util.ImageValidator;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.entity.UserProfileImage;
import com.anonymouschat.anonymouschatserver.domain.repository.UserProfileImageRepository;
import com.anonymouschat.anonymouschatserver.domain.repository.UserRepository;
import com.anonymouschat.anonymouschatserver.domain.type.Role;
import com.anonymouschat.anonymouschatserver.infra.file.FileStorage;
import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import com.anonymouschat.testsupport.util.TestUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.anonymouschat.testsupport.util.TestUtils.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

	@Mock private UserRepository userRepository;
	@Mock private UserProfileImageRepository userProfileImageRepository;
	@Mock private FileStorage fileStorage;
	@Mock private ImageValidator imageValidator;
	@InjectMocks private UserService userService;

	@Nested
	@DisplayName("회원가입")
	class RegisterUser {

		private UserServiceDto.RegisterCommand validCommand;
		private User guestUser;

		@BeforeEach
		void setUp() {
			validCommand = new UserServiceDto.RegisterCommand(
					"nickname",
					Gender.MALE,
					25,
					Region.SEOUL,
					"Hello!",
					OAuthProvider.GOOGLE,
					"google-id"
			);

			guestUser = TestUtils.guestUser();
			ReflectionTestUtils.setField(guestUser, "id", 1L);
		}

		@Test
		@DisplayName("이미지 없이 회원가입 성공")
		void registerUserWithoutImages() throws IOException {
			when(userRepository.findByProviderAndProviderIdAndActiveTrue(any(), any()))
					.thenReturn(Optional.of(guestUser));
			when(userRepository.existsByNickname(anyString())).thenReturn(false);

			Long id = userService.register(validCommand, List.of());

			assertThat(id).isEqualTo(1L);
			assertThat(guestUser.getNickname()).isEqualTo("nickname");
			assertThat(guestUser.getRole()).isEqualTo(Role.USER);
		}

		@Test
		@DisplayName("이미지 포함 회원가입 성공")
		void registerUserWithImages() throws IOException {
			MultipartFile image = mock(MultipartFile.class);

			when(userRepository.findByProviderAndProviderIdAndActiveTrue(any(), any()))
					.thenReturn(Optional.of(guestUser));
			when(fileStorage.upload(image)).thenReturn("https://image.com");
			when(userRepository.existsByNickname(anyString())).thenReturn(false);

			Long id = userService.register(validCommand, List.of(image));

			assertThat(id).isEqualTo(1L);
			verify(imageValidator).validate(image);
			verify(fileStorage).upload(image);
			assertThat(guestUser.getProfileImages()).hasSize(1);
			assertThat(guestUser.getProfileImages().getFirst().getImageUrl()).isEqualTo("https://image.com");
		}

		@Test
		@DisplayName("OAuth 임시 유저가 아닌 경우 예외 발생")
		void register_nonGuestUser_throwsException() throws Exception {
			User alreadyRegistered = TestUtils.createUser(3L);

			when(userRepository.findByProviderAndProviderIdAndActiveTrue(any(), any()))
					.thenReturn(Optional.of(alreadyRegistered));

			assertThatThrownBy(() -> userService.register(validCommand, List.of()))
					.isInstanceOf(BadRequestException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_REGISTERED_USER);
		}

		@Test
		@DisplayName("OAuth 임시 유저가 존재하지 않으면 예외 발생")
		void register_guestUserNotFound_throwsException() {
			when(userRepository.findByProviderAndProviderIdAndActiveTrue(any(), any()))
					.thenReturn(Optional.empty());

			assertThatThrownBy(() -> userService.register(validCommand, List.of()))
					.isInstanceOf(NotFoundException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_GUEST_NOT_FOUND);
		}

		@Test
		@DisplayName("닉네임 중복 시 예외 발생")
		void register_duplicateNickname_throwsException() {
			when(userRepository.findByProviderAndProviderIdAndActiveTrue(any(), any()))
					.thenReturn(Optional.of(guestUser));
			when(userRepository.existsByNickname("nickname")).thenReturn(true);

			assertThatThrownBy(() -> userService.register(validCommand, List.of()))
					.isInstanceOf(DuplicateNicknameException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NICKNAME);
		}

		@Test
		@DisplayName("이미지 유효성 검사 실패 시 예외 발생")
		void register_imageValidationFails_throwsException() {
			MultipartFile image = mock(MultipartFile.class);
			when(userRepository.findByProviderAndProviderIdAndActiveTrue(any(), any()))
					.thenReturn(Optional.of(guestUser));
			when(userRepository.existsByNickname(anyString())).thenReturn(false);
			doThrow(new UnsupportedImageFormatException(ErrorCode.UNSUPPORTED_IMAGE_FORMAT)).when(imageValidator).validate(image);

			assertThatThrownBy(() -> userService.register(validCommand, List.of(image)))
					.isInstanceOf(UnsupportedImageFormatException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNSUPPORTED_IMAGE_FORMAT);
		}

		@Test
		@DisplayName("파일 업로드 실패 시 예외 발생")
		void register_imageUploadFails_throwsIOException() throws IOException {
			MultipartFile image = mock(MultipartFile.class);
			when(userRepository.findByProviderAndProviderIdAndActiveTrue(any(), any()))
					.thenReturn(Optional.of(guestUser));
			when(userRepository.existsByNickname(anyString())).thenReturn(false);
			doNothing().when(imageValidator).validate(image);
			when(fileStorage.upload(image)).thenThrow(new IOException("upload fail"));

			assertThatThrownBy(() -> userService.register(validCommand, List.of(image)))
					.isInstanceOf(IOException.class)
					.hasMessage("upload fail");
		}
	}


	@Nested
	@DisplayName("프로필 조회")
	class GetProfile {

		@Test
		@DisplayName("정상적으로 프로필 조회")
		void getMyProfile_success() throws Exception {
			User user = createUser(1L);
			List<UserProfileImage> images = List.of(new UserProfileImage("url", true));
			ReflectionTestUtils.setField(user, "id", 1L);

			when(userRepository.findById(1L)).thenReturn(Optional.of(user));
			when(userProfileImageRepository.findAllByUserIdAndDeletedIsFalse(any(), any())).thenReturn(images);

			UserServiceDto.ProfileResult result = userService.getMyProfile(1L);

			assertThat(result.nickname()).isEqualTo(user.getNickname());
			assertThat(result.profileImages()).hasSize(1);
		}

		@Test
		@DisplayName("존재하지 않는 유저로 조회 시 실패")
		void getMyProfile_userNotFound() {
			when(userRepository.findById(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> userService.getMyProfile(999L))
					.isInstanceOf(UserNotFoundException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
		}
	}

	@Nested
	@DisplayName("회원 수정")
	class UpdateUser {

		@Test
		@DisplayName("회원 정보 수정 성공")
		void updateUser_success() throws Exception {
			User user = createUser(1L);
			ReflectionTestUtils.setField(user, "id", 1L);
			UserServiceDto.UpdateCommand update = new UserServiceDto.UpdateCommand(
					1L, "newNick", Gender.MALE, 30, Region.SEOUL, "bio"
			);
			MultipartFile image = mock(MultipartFile.class);

			when(userRepository.findById(1L)).thenReturn(Optional.of(user));
			when(userRepository.existsByNickname(anyString())).thenReturn(false);
			when(fileStorage.upload(any())).thenReturn("url");

			userService.update(update, List.of(image));

			verify(imageValidator).validate(image);
			verify(fileStorage).upload(image);
		}

		@Test
		@DisplayName("회원 정보 수정 실패 - 존재하지 않는 사용자")
		void updateUser_userNotFound() {
			when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

			assertThatThrownBy(() -> userService.update(
					new UserServiceDto.UpdateCommand(99L, "n", Gender.MALE, 20, Region.SEOUL, "bio"), List.of()
			)).isInstanceOf(UserNotFoundException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
		}

		@Test
		@DisplayName("회원 정보 수정 실패 - 이미지 유효성 오류")
		void updateUser_imageValidationFails() throws Exception {
			User user = createUser(1L);
			ReflectionTestUtils.setField(user, "id", 1L);
			MultipartFile image = mock(MultipartFile.class);

			when(userRepository.findById(1L)).thenReturn(Optional.of(user));
			when(userRepository.existsByNickname(anyString())).thenReturn(false);
			doThrow(new UnsupportedImageFormatException(ErrorCode.UNSUPPORTED_IMAGE_FORMAT)).when(imageValidator).validate(image);

			assertThatThrownBy(() -> userService.update(
					new UserServiceDto.UpdateCommand(1L, "n", Gender.MALE, 20, Region.SEOUL, "bio"),
					List.of(image)
			)).isInstanceOf(UnsupportedImageFormatException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNSUPPORTED_IMAGE_FORMAT);
		}
	}

	@Nested
	@DisplayName("회원 탈퇴")
	class WithdrawUser {

		@Test
		@DisplayName("회원 탈퇴 성공")
		void withdrawUser_success() throws Exception {
			User user = createUser(1L);
			when(userRepository.findById(1L)).thenReturn(Optional.of(user));

			userService.withdraw(1L);

			assertThat(user.isActive()).isFalse();
		}

		@Test
		@DisplayName("회원 탈퇴 실패 - 사용자 없음")
		void withdrawUser_userNotFound() {
			when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

			assertThatThrownBy(() -> userService.withdraw(123L))
					.isInstanceOf(UserNotFoundException.class)
					.hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
		}
	}

	@Test
	@DisplayName("닉네임 중복 검사")
	void checkNicknameDuplicate() {
		when(userRepository.existsByNickname("duplicate")).thenReturn(true);

		assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(
				userService, "validateNicknameDuplication", "duplicate"))
				.isInstanceOf(DuplicateNicknameException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NICKNAME);
	}
}