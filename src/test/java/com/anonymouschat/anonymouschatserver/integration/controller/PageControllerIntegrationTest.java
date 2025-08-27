package com.anonymouschat.anonymouschatserver.integration.controller;

import com.anonymouschat.anonymouschatserver.integration.config.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestConfig.class)
public class PageControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Nested
	@DisplayName("페이지 라우팅 테스트")
	class PageRoutingTest {

		@Test
		@DisplayName("로그인 페이지 접근")
		void loginPage_ShouldReturnLoginView() throws Exception {
			mockMvc.perform(get("/login"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(view().name("/pages/login"));
		}

		@Test
		@DisplayName("회원가입 페이지 접근")
		void registerPage_ShouldReturnRegisterView() throws Exception {
			mockMvc.perform(get("/register"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(view().name("/pages/register"));
		}

		@Test
		@DisplayName("메인 페이지 접근")
		void mainPage_ShouldReturnIndexView() throws Exception {
			mockMvc.perform(get("/"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(view().name("/pages/index"));
		}

		@Test
		@DisplayName("채팅 페이지 접근 - roomId 포함")
		void chatPage_WithRoomId_ShouldReturnChatViewWithModel() throws Exception {
			// Given
			Long roomId = 12345L;

			// When & Then
			mockMvc.perform(get("/chat/{roomId}", roomId))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(view().name("/pages/chat"))
					.andExpect(model().attribute("roomId", roomId));
		}

		@Test
		@DisplayName("채팅 목록 페이지 접근")
		void chatListPage_ShouldReturnChatListView() throws Exception {
			mockMvc.perform(get("/chat-list"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(view().name("/pages/chat-list"));
		}

		@Test
		@DisplayName("프로필 페이지 접근")
		void profilePage_ShouldReturnProfileView() throws Exception {
			mockMvc.perform(get("/profile"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(view().name("/pages/profile"));
		}

		@Test
		@DisplayName("프로필 수정 페이지 접근")
		void editProfilePage_ShouldReturnEditProfileView() throws Exception {
			mockMvc.perform(get("/profile/edit"))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(view().name("/pages/edit-profile"));
		}

		@Test
		@DisplayName("사용자 프로필 페이지 접근 - userId 포함")
		void userProfilePage_WithUserId_ShouldReturnUserProfileViewWithModel() throws Exception {
			// Given
			Long userId = 67890L;

			// When & Then
			mockMvc.perform(get("/user/{userId}", userId))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(view().name("/pages/user-profile"))
					.andExpect(model().attribute("userId", userId));
		}
	}

	@Nested
	@DisplayName("잘못된 페이지 경로 테스트")
	class InvalidPageRoutingTest {

		@Test
		@DisplayName("존재하지 않는 페이지 접근 시 401")
		void nonExistentPage_ShouldReturn404() throws Exception {
			mockMvc.perform(get("/non-existent-page"))
					.andDo(print())
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("잘못된 파라미터 타입으로 채팅 페이지 접근 시 400")
		void chatPage_WithInvalidRoomIdType_ShouldReturn400() throws Exception {
			mockMvc.perform(get("/chat/invalid-id"))
					.andDo(print())
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("잘못된 파라미터 타입으로 사용자 프로필 페이지 접근 시 400")
		void userProfilePage_WithInvalidUserIdType_ShouldReturn400() throws Exception {
			mockMvc.perform(get("/user/invalid-id"))
					.andDo(print())
					.andExpect(status().isBadRequest());
		}
	}
}