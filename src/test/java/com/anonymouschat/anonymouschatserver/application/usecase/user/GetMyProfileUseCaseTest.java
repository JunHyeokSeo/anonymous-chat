package com.anonymouschat.anonymouschatserver.application.usecase.user;

import com.anonymouschat.anonymouschatserver.application.dto.GetMyProfileResult;
import com.anonymouschat.anonymouschatserver.application.service.user.UserService;
import com.anonymouschat.anonymouschatserver.application.dto.GetMyProfileResult;
import com.anonymouschat.anonymouschatserver.domain.user.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GetMyProfileUseCaseTest {

	private final UserService userService = mock(UserService.class);
	private final GetMyProfileUseCase useCase = new GetMyProfileUseCase(userService);

	@Test
	@DisplayName("유스케이스 - 사용자 프로필 조회")
	void getMyProfileUseCase_success() {
		// given
		User user = new User(OAuthProvider.GOOGLE, "abc123", "사용자", Gender.FEMALE, 30, Region.BUSAN, "소개");
		ReflectionTestUtils.setField(user, "id", 10L);

		GetMyProfileResult mockResponse = GetMyProfileResult.builder()
				                                           .id(user.getId())
				                                           .provider(user.getProvider())
				                                           .providerId(user.getProviderId())
				                                           .nickname(user.getNickname())
				                                           .gender(user.getGender())
				                                           .age(user.getAge())
				                                           .region(user.getRegion())
				                                           .bio(user.getBio())
				                                           .createdAt(LocalDateTime.now())
				                                           .profileImages(List.of())
				                                           .build();

		when(userService.getMyProfile(OAuthProvider.GOOGLE, "abc123")).thenReturn(mockResponse);

		// when
		GetMyProfileResult result = useCase.getMyProfile(OAuthProvider.GOOGLE, "abc123");

		// then
		assertThat(result.nickname()).isEqualTo("사용자");
		assertThat(result.region()).isEqualTo(Region.BUSAN);
		verify(userService).getMyProfile(OAuthProvider.GOOGLE, "abc123");
	}
}
