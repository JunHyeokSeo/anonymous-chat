package com.anonymouschat.anonymouschatserver.application.service.user;

import com.anonymouschat.anonymouschatserver.application.dto.GetMyProfileResult;
import com.anonymouschat.anonymouschatserver.application.dto.RegisterUserCommand;
import com.anonymouschat.anonymouschatserver.application.dto.RegisterUserCommand;
import com.anonymouschat.anonymouschatserver.common.util.ImageValidator;
import com.anonymouschat.anonymouschatserver.domain.user.*;
import com.anonymouschat.anonymouschatserver.infra.file.FileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserServiceTest {

	private UserRepository userRepository;
	private UserProfileImageRepository userProfileImageRepository;
	private FileStorage fileStorage;
	private ImageValidator imageValidator;
	private UserService userService;

	@BeforeEach
	void setUp() {
		userRepository = mock(UserRepository.class);
		userProfileImageRepository = mock(UserProfileImageRepository.class);
		fileStorage = mock(FileStorage.class);
		imageValidator = mock(ImageValidator.class);
		userService = new UserService(userRepository, userProfileImageRepository, fileStorage, imageValidator);
	}

	@Test
	@DisplayName("유저 등록 - 이미지 없이")
	void registerUserWithoutImages() throws IOException {
		// given
		RegisterUserCommand request = new RegisterUserCommand(
				"testNick", Gender.MALE, 25, Region.SEOUL, "안녕하세요", OAuthProvider.GOOGLE, "providerId123"
		);

		User savedUser = mock(User.class);
		when(userRepository.save(any(User.class))).thenReturn(savedUser);
		when(savedUser.getId()).thenReturn(1L);

		// when
		Long userId = userService.register(request, List.of());

		// then
		assertThat(userId).isEqualTo(1L);
		verify(userRepository).save(any(User.class));
	}

	@Test
	@DisplayName("유저 등록 - 이미지 포함")
	void registerUserWithImages() throws IOException {
		// given
		RegisterUserCommand request = new RegisterUserCommand(
				"testNick", Gender.FEMALE, 30, Region.BUSAN, "Hello!", OAuthProvider.APPLE, "appleId123"
		);

		MultipartFile image1 = mock(MultipartFile.class);
		MultipartFile image2 = mock(MultipartFile.class);
		List<MultipartFile> images = List.of(image1, image2);

		when(fileStorage.upload(image1)).thenReturn("url1");
		when(fileStorage.upload(image2)).thenReturn("url2");

		User savedUser = mock(User.class);
		when(userRepository.save(any(User.class))).thenReturn(savedUser);
		when(savedUser.getId()).thenReturn(2L);
		doNothing().when(imageValidator).validate(any(MultipartFile.class));

		// when
		Long userId = userService.register(request, images);

		// then
		assertThat(userId).isEqualTo(2L);
		verify(fileStorage).upload(image1);
		verify(fileStorage).upload(image2);
		verify(userRepository).save(any(User.class));
	}

	@Test
	@DisplayName("닉네임 중복 검사")
	void checkNicknameDuplicate() {
		// given
		String nickname = "duplicateNick";
		when(userRepository.existsByNickname(nickname)).thenReturn(true);

		// when
		boolean result = userService.checkNicknameDuplicate(nickname);

		// then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("사용자 프로필 조회 - 정상 케이스")
	void getMyProfile_success() {
		// given
		User user = new User(OAuthProvider.GOOGLE, "providerId123", "닉네임", Gender.MALE, 25, Region.SEOUL, "소개");
		ReflectionTestUtils.setField(user, "id", 1L);

		List<UserProfileImage> images = List.of(
				new UserProfileImage("https://image.com/1.jpg", true),
				new UserProfileImage("https://image.com/2.jpg", false)
		);

		when(userRepository.findByProviderAndProviderId(OAuthProvider.GOOGLE, "providerId123"))
				.thenReturn(Optional.of(user));
		when(userProfileImageRepository.findAllByUserIdAndDeletedIsFalse(eq(1L), any(Sort.class)))
				.thenReturn(images);

		// when
		GetMyProfileResult response = userService.getMyProfile(OAuthProvider.GOOGLE, "providerId123");

		// then
		assertThat(response.nickname()).isEqualTo("닉네임");
		assertThat(response.profileImages()).hasSize(2);
		verify(userRepository, times(1)).findByProviderAndProviderId(any(), any());
		verify(userProfileImageRepository, times(1)).findAllByUserIdAndDeletedIsFalse(any(), any());
	}
}
