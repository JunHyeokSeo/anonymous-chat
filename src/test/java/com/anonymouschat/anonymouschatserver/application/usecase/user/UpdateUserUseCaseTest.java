package com.anonymouschat.anonymouschatserver.application.usecase.user;

import com.anonymouschat.anonymouschatserver.application.dto.UpdateUserCommand;
import com.anonymouschat.anonymouschatserver.application.dto.UpdateUserResult;
import com.anonymouschat.anonymouschatserver.application.service.user.UserService;
import com.anonymouschat.anonymouschatserver.domain.user.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.type.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UpdateUserUseCaseTest {

	@Mock
	private UserService userService;

	@InjectMocks
	private UpdateUserUseCase updateUserUseCase;

	public UpdateUserUseCaseTest() {
		MockitoAnnotations.openMocks(this);
	}

	private final UpdateUserCommand command = new UpdateUserCommand(
			1L, "newNick", Gender.MALE, 28, Region.SEOUL, "소개입니다."
	);

	@Test
	@DisplayName("정상적으로 유저 정보를 업데이트한다")
	void update_success() throws IOException {
		// given
		MultipartFile image1 = mock(MultipartFile.class);
		MultipartFile image2 = mock(MultipartFile.class);
		List<MultipartFile> images = List.of(image1, image2);

		UpdateUserResult expectedResult = mock(UpdateUserResult.class);
		when(userService.update(command, images)).thenReturn(expectedResult);

		// when
		UpdateUserResult result = updateUserUseCase.update(command, images);

		// then
		assertThat(result).isEqualTo(expectedResult);
		verify(userService).update(command, images);
	}

	@Test
	@DisplayName("이미지가 3장을 초과하면 예외가 발생한다")
	void update_throws_whenImageSizeExceedsLimit() {
		// given
		List<MultipartFile> images = List.of(
				mock(MultipartFile.class),
				mock(MultipartFile.class),
				mock(MultipartFile.class),
				mock(MultipartFile.class)
		);

		// when & then
		IllegalStateException exception = assertThrows(
				IllegalStateException.class,
				() -> updateUserUseCase.update(command, images)
		);

		assertThat(exception.getMessage()).isEqualTo("이미지는 최대 3장까지 업로드할 수 있습니다.");
		verifyNoInteractions(userService);
	}

	@Test
	@DisplayName("UserService에서 IOException이 발생하면 그대로 전파된다")
	void update_throws_whenServiceThrowsIOException() throws IOException {
		// given
		List<MultipartFile> images = List.of(mock(MultipartFile.class));
		when(userService.update(command, images)).thenThrow(new IOException("업로드 실패"));

		// when & then
		IOException exception = assertThrows(
				IOException.class,
				() -> updateUserUseCase.update(command, images)
		);

		assertThat(exception.getMessage()).isEqualTo("업로드 실패");
		verify(userService).update(command, images);
	}
}
