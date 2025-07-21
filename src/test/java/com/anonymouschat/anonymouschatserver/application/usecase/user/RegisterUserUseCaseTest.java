
package com.anonymouschat.anonymouschatserver.application.usecase.user;

import com.anonymouschat.anonymouschatserver.application.service.dto.RegisterUserServiceCommand;
import com.anonymouschat.anonymouschatserver.application.service.user.UserService;
import com.anonymouschat.anonymouschatserver.application.usecase.user.dto.RegisterUserUseCaseCommand;
import com.anonymouschat.anonymouschatserver.domain.user.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.user.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

	@Mock
	private UserService userService;

	@InjectMocks
	private RegisterUserUseCase registerUserUseCase;

	private RegisterUserUseCaseCommand createCommand(String nickname) {
		return new RegisterUserUseCaseCommand(nickname, Gender.FEMALE, 25, Region.SEOUL, "소개", OAuthProvider.APPLE, "testId");
	}

	@Test
	@DisplayName("정상적으로 유저 등록에 성공하면 userId를 반환한다")
	void registerUserSuccessfully() throws IOException {
		RegisterUserUseCaseCommand command = createCommand("uniqueNick");

		when(userService.checkNicknameDuplicate("uniqueNick")).thenReturn(false);
		when(userService.register(any(RegisterUserServiceCommand.class), any())).thenReturn(1L);

		Long userId = registerUserUseCase.register(command, List.of());

		assertThat(userId).isEqualTo(1L);
	}

	@Test
	@DisplayName("중복된 닉네임일 경우 예외가 발생한다")
	void shouldThrowWhenNicknameIsDuplicated() {
		RegisterUserUseCaseCommand command = createCommand("duplicateNick");

		when(userService.checkNicknameDuplicate("duplicateNick")).thenReturn(true);

		assertThrows(IllegalStateException.class, () -> {
			registerUserUseCase.register(command, List.of());
		});
	}

	@Test
	@DisplayName("이미지가 3장을 초과하면 예외가 발생한다")
	void shouldThrowWhenImagesExceedLimit() {
		RegisterUserUseCaseCommand command = createCommand("someNick");

		List<MultipartFile> images = List.of(
				mock(MultipartFile.class),
				mock(MultipartFile.class),
				mock(MultipartFile.class),
				mock(MultipartFile.class)
		);

		assertThrows(IllegalStateException.class, () -> {
			registerUserUseCase.register(command, images);
		});
	}
}
