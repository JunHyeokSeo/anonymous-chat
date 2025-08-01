package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.application.dto.UserServiceDto;
import com.anonymouschat.anonymouschatserver.common.util.ImageValidator;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.entity.UserProfileImage;
import com.anonymouschat.anonymouschatserver.domain.repository.UserProfileImageRepository;
import com.anonymouschat.anonymouschatserver.domain.repository.UserRepository;
import com.anonymouschat.anonymouschatserver.common.file.FileStorage;
import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
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
		}

		@Test
		@DisplayName("이미지 없이 회원가입 성공")
		void registerUserWithoutImages() throws IOException {
			when(userRepository.existsByNickname(anyString())).thenReturn(false);
			when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
				User saved = invocation.getArgument(0);
				ReflectionTestUtils.setField(saved, "id", 1L);
				return saved;
			});

			Long id = userService.register(validCommand, List.of());

			assertThat(id).isEqualTo(1L);
		}

		@Test
		@DisplayName("이미지 포함 회원가입 성공")
		void registerUserWithImages() throws IOException {
			MultipartFile image = mock(MultipartFile.class);
			when(userRepository.existsByNickname(anyString())).thenReturn(false);
			when(fileStorage.upload(any())).thenReturn("https://image.com");
			when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
				User saved = invocation.getArgument(0);
				ReflectionTestUtils.setField(saved, "id", 2L);
				return saved;
			});

			Long id = userService.register(validCommand, List.of(image));

			assertThat(id).isEqualTo(2L);
			verify(imageValidator).validate(image);
			verify(fileStorage).upload(image);
		}

		@Test
		@DisplayName("이미지 유효성 검사 실패로 회원가입 실패")
		void registerUser_imageValidationFails() {
			MultipartFile image = mock(MultipartFile.class);
			doThrow(new IllegalArgumentException("잘못된 이미지")).when(imageValidator).validate(image);

			when(userRepository.existsByNickname(anyString())).thenReturn(false);

			assertThatThrownBy(() -> userService.register(validCommand, List.of(image)))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		@DisplayName("파일 업로드 실패로 회원가입 실패")
		void registerUser_uploadFails() throws IOException {
			MultipartFile image = mock(MultipartFile.class);
			doNothing().when(imageValidator).validate(image);
			when(fileStorage.upload(image)).thenThrow(new IOException("업로드 실패"));
			when(userRepository.existsByNickname(anyString())).thenReturn(false);

			assertThatThrownBy(() -> userService.register(validCommand, List.of(image)))
					.isInstanceOf(IOException.class);
		}
	}

	@Nested
	@DisplayName("프로필 조회")
	class GetProfile {

		@Test
		@DisplayName("정상적으로 프로필 조회")
		void getMyProfile_success() {
			User user = createUser();
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
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("사용자를 찾을 수 없습니다");
		}
	}

	@Nested
	@DisplayName("회원 수정")
	class UpdateUser {

		@Test
		@DisplayName("회원 정보 수정 성공")
		void updateUser_success() throws IOException {
			User user = createUser();
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
			)).isInstanceOf(IllegalStateException.class);
		}

		@Test
		@DisplayName("회원 정보 수정 실패 - 이미지 유효성 오류")
		void updateUser_imageValidationFails() {
			User user = createUser();
			ReflectionTestUtils.setField(user, "id", 1L);
			MultipartFile image = mock(MultipartFile.class);

			when(userRepository.findById(1L)).thenReturn(Optional.of(user));
			when(userRepository.existsByNickname(anyString())).thenReturn(false);
			doThrow(new IllegalArgumentException("invalid")).when(imageValidator).validate(image);

			assertThatThrownBy(() -> userService.update(
					new UserServiceDto.UpdateCommand(1L, "n", Gender.MALE, 20, Region.SEOUL, "bio"),
					List.of(image)
			)).isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Nested
	@DisplayName("회원 탈퇴")
	class WithdrawUser {

		@Test
		@DisplayName("회원 탈퇴 성공")
		void withdrawUser_success() {
			User user = createUser();
			when(userRepository.findById(1L)).thenReturn(Optional.of(user));

			userService.withdraw(1L);

			assertThat(user.isActive()).isFalse();
		}

		@Test
		@DisplayName("회원 탈퇴 실패 - 사용자 없음")
		void withdrawUser_userNotFound() {
			when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

			assertThatThrownBy(() -> userService.withdraw(123L))
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("사용자를 찾을 수 없습니다");
		}
	}

	@Test
	@DisplayName("닉네임 중복 검사")
	void checkNicknameDuplicate() {
		when(userRepository.existsByNickname("duplicate")).thenReturn(true);

		assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(
				userService, "validateNicknameDuplication", "duplicate"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("동일한 닉네임이 존재합니다.");
	}

	private User createUser() {
		return User.builder()
				       .provider(OAuthProvider.GOOGLE)
				       .providerId("sub")
				       .nickname("nick")
				       .gender(Gender.MALE)
				       .age(20)
				       .region(Region.SEOUL)
				       .bio("intro")
				       .build();
	}

}
