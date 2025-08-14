package com.anonymouschat.anonymouschatserver.application.service;

import com.anonymouschat.anonymouschatserver.application.dto.UserServiceDto;
import com.anonymouschat.anonymouschatserver.domain.repository.UserRepository;
import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSearchService 테스트")
class UserSearchServiceTest {

	@InjectMocks
	private UserSearchService userSearchService;

	@Mock
	private UserRepository userRepository;

	private UserServiceDto.SearchCommand command;
	private Pageable pageable;

	@BeforeEach
	void setUp() {
		command = UserServiceDto.SearchCommand.builder()
				          .id(1L)
				          .gender(Gender.MALE)
				          .minAge(20)
				          .maxAge(30)
				          .region(Region.SEOUL)
				          .blockedUserIds(List.of(99L))
				          .build();

		pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lastActiveAt"));
	}

	@Nested
	@DisplayName("search 메서드는")
	class Describe_search {

		@Test
		@DisplayName("조건에 맞는 사용자 목록을 Slice 형태로 반환한다")
		void it_returns_search_results() {
			// given
			List<UserServiceDto.SearchResult> results = List.of(
					new UserServiceDto.SearchResult(
							2L,
							"nickname",
							Gender.FEMALE,
							25,
							Region.SEOUL,
							"image-url",
							LocalDateTime.now()
					)
			);
			Slice<UserServiceDto.SearchResult> mockSlice =
					new SliceImpl<>(results, pageable, false);

			when(userRepository.searchUsers(command, pageable))
					.thenReturn(mockSlice);

			// when
			Slice<UserServiceDto.SearchResult> slice = userSearchService.search(command, pageable);

			// then
			assertThat(slice).isNotNull();
			assertThat(slice.getContent()).hasSize(1);
			assertThat(slice.getContent().get(0).nickname()).isEqualTo("nickname");
			verify(userRepository).searchUsers(command, pageable);
		}

		@Test
		@DisplayName("조건에 맞는 사용자가 없으면 빈 Slice를 반환한다")
		void it_returns_empty_slice_if_no_results() {
			// given
			Slice<UserServiceDto.SearchResult> emptySlice =
					new SliceImpl<>(List.of(), pageable, false);

			when(userRepository.searchUsers(any(), any()))
					.thenReturn(emptySlice);

			// when
			Slice<UserServiceDto.SearchResult> slice = userSearchService.search(command, pageable);

			// then
			assertThat(slice.getContent()).isEmpty();
			verify(userRepository).searchUsers(command, pageable);
		}
	}
}
