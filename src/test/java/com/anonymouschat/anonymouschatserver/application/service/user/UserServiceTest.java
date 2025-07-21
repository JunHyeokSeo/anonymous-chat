package com.anonymouschat.anonymouschatserver.application.service.user;

import com.anonymouschat.anonymouschatserver.application.dto.*;
import com.anonymouschat.anonymouschatserver.common.util.ImageValidator;
import com.anonymouschat.anonymouschatserver.domain.user.entity.User;
import com.anonymouschat.anonymouschatserver.domain.user.entity.UserProfileImage;
import com.anonymouschat.anonymouschatserver.domain.user.repository.UserProfileImageRepository;
import com.anonymouschat.anonymouschatserver.domain.user.repository.UserRepository;
import com.anonymouschat.anonymouschatserver.domain.user.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.user.type.Region;
import com.anonymouschat.anonymouschatserver.infra.file.FileStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock private UserRepository userRepository;
	@Mock private UserProfileImageRepository userProfileImageRepository;
	@Mock private FileStorage fileStorage;
	@Mock private ImageValidator imageValidator;
	@InjectMocks private UserService userService;

	private final RegisterUserCommand baseRegisterUserCommand =
			new RegisterUserCommand("testNick", Gender.MALE, 25, Region.SEOUL, "안녕하세요", OAuthProvider.GOOGLE, "providerId123");

	private User createMockUser(Long id, String nickname) {
		User user = new User(OAuthProvider.GOOGLE, "providerId123", nickname, Gender.MALE, 25, Region.SEOUL, "bio");
		ReflectionTestUtils.setField(user, "id", id);
		return user;
	}

	@Test
	@DisplayName("유저 등록 - 이미지 없이")
	void registerUserWithoutImages() throws IOException {
		User savedUser = mock(User.class);
		when(userRepository.save(any(User.class))).thenReturn(savedUser);
		when(savedUser.getId()).thenReturn(1L);

		Long userId = userService.register(baseRegisterUserCommand, List.of());

		assertThat(userId).isEqualTo(1L);
		verify(userRepository).save(any(User.class));
	}

	@Test
	@DisplayName("유저 등록 - 이미지 포함")
	void registerUserWithImages() throws IOException {
		MultipartFile image1 = mock(MultipartFile.class);
		MultipartFile image2 = mock(MultipartFile.class);
		List<MultipartFile> images = List.of(image1, image2);

		when(fileStorage.upload(image1)).thenReturn("url1");
		when(fileStorage.upload(image2)).thenReturn("url2");

		User savedUser = mock(User.class);
		when(userRepository.save(any(User.class))).thenReturn(savedUser);
		when(savedUser.getId()).thenReturn(2L);
		doNothing().when(imageValidator).validate(any(MultipartFile.class));

		Long userId = userService.register(baseRegisterUserCommand, images);

		assertThat(userId).isEqualTo(2L);
		verify(fileStorage).upload(image1);
		verify(fileStorage).upload(image2);
		verify(userRepository).save(any(User.class));
	}

	@Test
	@DisplayName("유저 등록 실패 - 이미지 유효성 검사 실패")
	void registerUser_imageValidationFails() throws IOException {
		// given
		MultipartFile image = mock(MultipartFile.class);
		List<MultipartFile> images = List.of(image);

		doThrow(new IllegalArgumentException("잘못된 이미지입니다."))
				.when(imageValidator).validate(image);

		// when & then
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> userService.register(baseRegisterUserCommand, images)
		);

		assertThat(exception.getMessage()).isEqualTo("잘못된 이미지입니다.");
		verify(imageValidator).validate(image);
		verify(fileStorage, never()).upload(any());
	}

	@Test
	@DisplayName("유저 등록 실패 - 파일 업로드 중 IOException 발생")
	void registerUser_uploadFails() throws IOException {
		// given
		MultipartFile image = mock(MultipartFile.class);
		List<MultipartFile> images = List.of(image);

		doNothing().when(imageValidator).validate(any());
		when(fileStorage.upload(image)).thenThrow(new IOException("S3 업로드 실패"));

		// when & then
		IOException exception = assertThrows(
				IOException.class,
				() -> userService.register(baseRegisterUserCommand, images)
		);

		assertThat(exception.getMessage()).isEqualTo("S3 업로드 실패");
		verify(fileStorage).upload(image);
	}


	@Test
	@DisplayName("닉네임 중복 검사")
	void checkNicknameDuplicate() {
		when(userRepository.existsByNickname("duplicateNick")).thenReturn(true);

		boolean result = userService.checkNicknameDuplicate("duplicateNick");

		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("사용자 프로필 조회 - 정상 케이스")
	void getMyProfile_success() {
		User user = createMockUser(1L, "닉네임");
		List<UserProfileImage> images = List.of(
				new UserProfileImage("https://image.com/1.jpg", true),
				new UserProfileImage("https://image.com/2.jpg", false)
		);

		when(userRepository.findByProviderAndProviderId(OAuthProvider.GOOGLE, "providerId123")).thenReturn(Optional.of(user));
		when(userProfileImageRepository.findAllByUserIdAndDeletedIsFalse(eq(1L), any(Sort.class))).thenReturn(images);

		GetMyProfileResult response = userService.getMyProfile(OAuthProvider.GOOGLE, "providerId123");

		assertThat(response.nickname()).isEqualTo("닉네임");
		assertThat(response.profileImages()).hasSize(2);
	}

	@Test
	@DisplayName("프로필 조회 실패 - 사용자 없음")
	void getMyProfile_userNotFound() {
		// given
		when(userRepository.findByProviderAndProviderId(any(), any()))
				.thenReturn(Optional.empty());

		// when & then
		IllegalStateException exception = assertThrows(
				IllegalStateException.class,
				() -> userService.getMyProfile(OAuthProvider.GOOGLE, "missing-user-id")
		);

		assertThat(exception.getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
	}


	@Test
	@DisplayName("유저 정보 수정 성공")
	void updateUser_success() throws IOException {
		Long userId = 1L;
		User user = createMockUser(userId, "oldNick");
		UpdateUserCommand command = new UpdateUserCommand(userId, "newNick", Gender.MALE, 28, Region.BUSAN, "new bio");

		MultipartFile image = mock(MultipartFile.class);
		List<MultipartFile> images = List.of(image);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userProfileImageRepository.findAllByUserIdAndDeletedIsFalse(userId)).thenReturn(new ArrayList<>());
		when(fileStorage.upload(any())).thenReturn("http://image.url");

		var result = userService.update(command, images);

		assertThat(result.nickname()).isEqualTo("newNick");
		assertThat(result.age()).isEqualTo(28);
		assertThat(result.region()).isEqualTo(Region.BUSAN);
		assertThat(result.bio()).isEqualTo("new bio");
	}

	@Test
	@DisplayName("유저 정보 수정 실패 - 존재하지 않는 사용자")
	void updateUser_userNotFound() {
		Long userId = 999L;
		UpdateUserCommand command = new UpdateUserCommand(userId, "nick", Gender.MALE, 30, Region.SEOUL, "bio");

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThrows(IllegalStateException.class, () -> userService.update(command, List.of()));
	}

	@Test
	@DisplayName("유저 수정 실패 - 이미지 유효성 검사 실패")
	void updateUser_imageValidationFails() throws IOException {
		// given
		Long userId = 1L;
		User user = createMockUser(userId, "oldNick");
		MultipartFile image = mock(MultipartFile.class);
		List<MultipartFile> images = List.of(image);

		UpdateUserCommand command = new UpdateUserCommand(
				userId, "newNick", Gender.FEMALE, 30, Region.SEOUL, "bio"
		);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userProfileImageRepository.findAllByUserIdAndDeletedIsFalse(userId))
				.thenReturn(new ArrayList<>());
		doThrow(new IllegalArgumentException("이미지 형식 오류"))
				.when(imageValidator).validate(image);

		// when & then
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> userService.update(command, images)
		);

		assertThat(exception.getMessage()).isEqualTo("이미지 형식 오류");
		verify(fileStorage, never()).upload(any());
	}
}
