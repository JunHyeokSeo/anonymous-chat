package com.anonymouschat.anonymouschatserver.application.usecase.user;

import com.anonymouschat.anonymouschatserver.application.service.user.UserService;
import com.anonymouschat.anonymouschatserver.application.usecase.user.dto.RegisterUserCommand;
import com.anonymouschat.anonymouschatserver.common.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class RegisterUserUseCase {
	private final UserService userService;

	public Long register(RegisterUserCommand command, List<MultipartFile> images) throws IOException {
		if (images != null && images.size() > 3) {
			throw new IllegalStateException("이미지는 최대 3장까지 업로드할 수 있습니다.");
		}

		if (userService.checkNicknameDuplicate(command.nickname()))
			throw new IllegalStateException("동일한 닉네임을 가진 사용자가 있습니다.");

		return userService.register(command.toServiceRequest(), images);
	}
}
