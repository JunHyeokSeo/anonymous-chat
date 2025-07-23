package com.anonymouschat.anonymouschatserver.application.usecase.user;

import com.anonymouschat.anonymouschatserver.application.service.user.UserService;
import com.anonymouschat.anonymouschatserver.common.annotation.UseCase;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class WithdrawUserUseCase {
	private final UserService userService;

	public void withDraw(Long userId) {
		userService.withdraw(userId);
	}
}
