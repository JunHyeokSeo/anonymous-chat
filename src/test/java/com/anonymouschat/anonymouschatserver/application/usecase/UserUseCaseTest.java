//
//package com.anonymouschat.anonymouschatserver.application.usecase;
//
//import com.anonymouschat.anonymouschatserver.application.dto.*;
//import com.anonymouschat.anonymouschatserver.application.service.BlockService;
//import com.anonymouschat.anonymouschatserver.application.service.UserSearchService;
//import com.anonymouschat.anonymouschatserver.application.service.UserService;
//import com.anonymouschat.anonymouschatserver.domain.type.Gender;
//import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
//import com.anonymouschat.anonymouschatserver.domain.type.Region;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Slice;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@DisplayName("UserUseCase 테스트")
//class UserUseCaseTest {
//
//	@Mock private UserService userService;
//	@Mock private UserSearchService userSearchService;
//	@Mock private BlockService blockService;
//	@InjectMocks private UserUseCase userUseCase;
//
//	private RegisterUserCommand registerCommand;
//	private UpdateUserCommand updateCommand;
//
//	@BeforeEach
//	void setUp() {
//		MockitoAnnotations.openMocks(this);
//		registerCommand = new RegisterUserCommand("닉네임", Gender.MALE, 30, Region.SEOUL, "소개", OAuthProvider.GOOGLE, "providerId");
//		updateCommand = new UpdateUserCommand(1L, "닉", Gender.FEMALE, 25, Region.BUSAN, "변경된 소개");
//	}
//
//	@Nested
//	@DisplayName("회원가입 테스트")
//	class Register {
//
//		@DisplayName("회원가입에 성공한다")
//		@Test
//		void register_success(){
//		}
//
//		@DisplayName("닉네임이 중복되면 회원가입에 실패한다")
//		@Test
//		void register_throws_whenNicknameDuplicated() {
//		}
//	}
//
//	@Nested
//	@DisplayName("회원 정보 수정 테스트")
//	class Update {
//
//		@DisplayName("회원 정보 수정에 성공한다")
//		@Test
//		void update_success(){
//
//		}
//
//		@DisplayName("수정 시 IOException이 발생하면 예외가 던져진다")
//		@Test
//		void update_throws_whenServiceThrowsIOException() {
//		}
//	}
//
//	@Nested
//	@DisplayName("프로필 조회 테스트")
//	class Profile {
//
//		@DisplayName("내 프로필 정보를 조회한다")
//		@Test
//		void getMyProfile_success() {
//			GetMyProfileResult expected = mock(GetMyProfileResult.class);
//			when(userService.getMyProfile(1L)).thenReturn(expected);
//
//			GetMyProfileResult result = userUseCase.getMyProfile(1L);
//			assertThat(result).isEqualTo(expected);
//		}
//	}
//
//	@Nested
//	@DisplayName("회원 탈퇴 테스트")
//	class Withdraw {
//
//		@DisplayName("회원 탈퇴에 성공한다")
//		@Test
//		void withdraw_success() {
//			doNothing().when(userService).withdraw(1L);
//			userUseCase.withdraw(1L);
//			verify(userService).withdraw(1L);
//		}
//	}
//
//	@Nested
//	@DisplayName("유저 검색 테스트")
//	class Search {
//
//		@DisplayName("유저 검색에 성공한다")
//		@Test
//		void search_success() {
//			List<Long> blockedIds = List.of(2L, 3L);
//			@SuppressWarnings("unchecked")
//			Slice<UserSearchResult> result = (Slice<UserSearchResult>) mock(Slice.class);
//			UserSearchCondition cond = new UserSearchCondition(Gender.MALE, 20, 30, Region.SEOUL);
//
//			when(blockService.getBlockedUserIds(1L)).thenReturn(blockedIds);
//			when(userSearchService.search(eq(1L), eq(cond), eq(blockedIds), any(Pageable.class))).thenReturn(result);
//
//			Slice<UserSearchResult> actual = userUseCase.search(1L, cond, Pageable.unpaged());
//
//			assertThat(actual).isEqualTo(result);
//		}
//	}
//}
//
//
