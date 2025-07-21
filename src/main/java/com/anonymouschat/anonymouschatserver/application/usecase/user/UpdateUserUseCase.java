package com.anonymouschat.anonymouschatserver.application.usecase.user;

import com.anonymouschat.anonymouschatserver.application.dto.UpdateUserCommand;
import com.anonymouschat.anonymouschatserver.application.dto.UpdateUserResult;
import com.anonymouschat.anonymouschatserver.application.service.user.UserService;
import com.anonymouschat.anonymouschatserver.common.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class UpdateUserUseCase {
	private final UserService userService;

	public UpdateUserResult update(UpdateUserCommand command, List<MultipartFile> images) throws IOException {
		if (images.size() > 3) {
			throw new IllegalStateException("이미지는 최대 3장까지 업로드할 수 있습니다.");
		}

		return userService.update(command, images);
	}
}
