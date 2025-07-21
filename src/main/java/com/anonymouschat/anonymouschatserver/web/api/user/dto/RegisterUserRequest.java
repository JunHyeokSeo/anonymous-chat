package com.anonymouschat.anonymouschatserver.web.api.user.dto;

import com.anonymouschat.anonymouschatserver.application.dto.RegisterUserCommand;
import com.anonymouschat.anonymouschatserver.domain.user.Gender;
import com.anonymouschat.anonymouschatserver.domain.user.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.user.Region;
import jakarta.validation.constraints.*;

public record RegisterUserRequest(

		@NotBlank(message = "닉네임은 필수 항목입니다.")
		String nickname,

		@NotNull(message = "성별을 선택해주세요.")
		Gender gender,

		@Min(value = 0, message = "나이는 0세 이상이어야 합니다.")
		@Max(value = 99, message = "나이는 99세 이하로 입력해주세요.")
		int age,

		@NotNull(message = "지역을 선택해주세요.")
		Region region,

		@Size(max = 255, message = "자기소개는 255자 이하로 입력해주세요.")
		String bio

) {
	public RegisterUserCommand toCommand(OAuthProvider provider, String providerId) {
		return new RegisterUserCommand(
				nickname, gender, age, region,
				bio == null ? "" : bio,
				provider, providerId
		);
	}
}
