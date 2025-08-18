
package com.anonymouschat.anonymouschatserver.application.usecase;

import com.anonymouschat.anonymouschatserver.application.dto.*;
import com.anonymouschat.anonymouschatserver.application.service.BlockService;
import com.anonymouschat.anonymouschatserver.application.service.UserSearchService;
import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("UserUseCase 테스트")
class UserUseCaseTest {

	@Mock private UserSearchService userSearchService;
	@Mock private BlockService blockService;
	@InjectMocks private UserUseCase userUseCase;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Nested
	@DisplayName("유저 검색 테스트")
	class Search {

		@DisplayName("조건 기반 유저 목록 조회에 성공한다")
		@Test
		void get_users_by_condition_success() {
			// given
			Long userId = 1L;
			UserUseCaseDto.SearchConditionRequest searchCondition = UserUseCaseDto.SearchConditionRequest.builder()
					                                                 .id(userId)
					                                                 .gender(Gender.FEMALE)
					                                                 .minAge(20)
					                                                 .maxAge(30)
					                                                 .region(Region.BUSAN)
					                                                 .build();
			List<Long> blockedUserIds = List.of(3L, 4L);

			UserServiceDto.SearchCommand serviceCommand = UserServiceDto.SearchCommand.from(searchCondition, blockedUserIds);
			UserServiceDto.SearchResult serviceResult = new UserServiceDto.SearchResult(2L, "상대닉네임", Gender.FEMALE, 25, Region.BUSAN, "url", LocalDateTime.now());
			Slice<UserServiceDto.SearchResult> serviceResultSlice = new SliceImpl<>(List.of(serviceResult));

			when(blockService.getBlockedUserIds(userId)).thenReturn(blockedUserIds);
			when(userSearchService.getUsersByCondition(serviceCommand, Pageable.unpaged())).thenReturn(serviceResultSlice);

			// when
			Slice<UserUseCaseDto.SearchResponse> result = userUseCase.getUserList(searchCondition, Pageable.unpaged());

			// then
			assertThat(result).hasSize(1);
			assertThat(result.getContent().getFirst().userId()).isEqualTo(2L);

			verify(blockService).getBlockedUserIds(userId);
			verify(userSearchService).getUsersByCondition(serviceCommand, Pageable.unpaged());
		}
	}
}


