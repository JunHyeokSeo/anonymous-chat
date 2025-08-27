package com.anonymouschat.anonymouschatserver.integration.controller;

import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import com.anonymouschat.anonymouschatserver.integration.common.IntegrationTestHelper;
import com.anonymouschat.anonymouschatserver.integration.config.TestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestConfig.class)
public class BlockControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private IntegrationTestHelper testHelper;

	@AfterEach
	void cleanup() {
		testHelper.cleanup();
	}

	@Nested
	@DisplayName("사용자 차단 API")
	class BlockUserTest {

		@Test
		@DisplayName("유효한 사용자 차단 성공")
		void blockUser_WithValidUserId_ShouldReturnSuccess() throws Exception {
			// Given
			var blocker = testHelper.createTestUser("blocker", Gender.MALE, 25, Region.SEOUL);
			var blocked = testHelper.createTestUser("blocked", Gender.FEMALE, 27, Region.BUSAN);

			var request = Map.of("blockedUserId", blocked.user().getId());

			// When & Then
			mockMvc.perform(testHelper.withAuth(post("/api/v1/blocks"), blocker.accessToken())
					                .contentType(MediaType.APPLICATION_JSON)
					                .content(testHelper.asJsonString(request)))
					.andDo(print())
					.andExpect(status().isCreated())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data").doesNotExist())
					.andExpect(jsonPath("$.error").doesNotExist());
		}

		@Test
		@DisplayName("자기 자신 차단 시 400 에러")
		void blockUser_WithSelfId_ShouldReturn400() throws Exception {
			// Given
			var user = testHelper.createTestUser("user", Gender.MALE, 25, Region.SEOUL);
			var request = Map.of("blockedUserId", user.user().getId());

			// When & Then
			mockMvc.perform(testHelper.withAuth(post("/api/v1/blocks"), user.accessToken())
					                .contentType(MediaType.APPLICATION_JSON)
					                .content(testHelper.asJsonString(request)))
					.andDo(print())
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("존재하지 않는 사용자 차단 시 404 에러")
		void blockUser_WithNonExistentUser_ShouldReturn404() throws Exception {
			// Given
			var blocker = testHelper.createTestUser("blocker", Gender.MALE, 25, Region.SEOUL);
			var request = Map.of("blockedUserId", 99999L);

			// When & Then
			mockMvc.perform(testHelper.withAuth(post("/api/v1/blocks"), blocker.accessToken())
					                .contentType(MediaType.APPLICATION_JSON)
					                .content(testHelper.asJsonString(request)))
					.andDo(print())
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("인증 없이 차단 요청 시 401 에러")
		void blockUser_WithoutAuth_ShouldReturn401() throws Exception {
			// Given
			var request = Map.of("blockedUserId", 1L);

			// When & Then
			mockMvc.perform(post("/api/v1/blocks")
					                .contentType(MediaType.APPLICATION_JSON)
					                .content(testHelper.asJsonString(request)))
					.andDo(print())
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("이미 차단된 사용자 재차단 시 정상 처리 - 멱등")
		void blockUser_AlreadyBlocked_ShouldReturn409() throws Exception {
			// Given
			var blocker = testHelper.createTestUser("blocker", Gender.MALE, 25, Region.SEOUL);
			var blocked = testHelper.createTestUser("blocked", Gender.FEMALE, 27, Region.BUSAN);
			var request = Map.of("blockedUserId", blocked.user().getId());

			// 첫 번째 차단
			mockMvc.perform(testHelper.withAuth(post("/api/v1/blocks"), blocker.accessToken())
					                .contentType(MediaType.APPLICATION_JSON)
					                .content(testHelper.asJsonString(request)))
					.andExpect(status().isCreated());

			// When & Then - 두 번째 차단 시도
			mockMvc.perform(testHelper.withAuth(post("/api/v1/blocks"), blocker.accessToken())
					                .contentType(MediaType.APPLICATION_JSON)
					                .content(testHelper.asJsonString(request)))
					.andDo(print())
					.andExpect(status().isCreated());
		}
	}

	@Nested
	@DisplayName("사용자 차단 해제 API")
	class UnblockUserTest {

		@Test
		@DisplayName("차단 해제 성공")
		void unblockUser_WithValidBlockedUser_ShouldReturnNoContent() throws Exception {
			// Given
			var blocker = testHelper.createTestUser("blocker", Gender.MALE, 25, Region.SEOUL);
			var blocked = testHelper.createTestUser("blocked", Gender.FEMALE, 27, Region.BUSAN);
			var request = Map.of("blockedUserId", blocked.user().getId());

			// 먼저 차단
			mockMvc.perform(testHelper.withAuth(post("/api/v1/blocks"), blocker.accessToken())
					                .contentType(MediaType.APPLICATION_JSON)
					                .content(testHelper.asJsonString(request)))
					.andExpect(status().isCreated());

			// When & Then - 차단 해제
			mockMvc.perform(testHelper.withAuth(delete("/api/v1/blocks"), blocker.accessToken())
					                .contentType(MediaType.APPLICATION_JSON)
					                .content(testHelper.asJsonString(request)))
					.andDo(print())
					.andExpect(status().isNoContent())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data").doesNotExist())
					.andExpect(jsonPath("$.error").doesNotExist());
		}

		@Test
		@DisplayName("차단하지 않은 사용자 해제 시 404 에러")
		void unblockUser_WithNonBlockedUser_ShouldReturn404() throws Exception {
			// Given
			var blocker = testHelper.createTestUser("blocker", Gender.MALE, 25, Region.SEOUL);
			var notBlocked = testHelper.createTestUser("notblocked", Gender.FEMALE, 27, Region.BUSAN);
			var request = Map.of("blockedUserId", notBlocked.user().getId());

			// When & Then
			mockMvc.perform(testHelper.withAuth(delete("/api/v1/blocks"), blocker.accessToken())
					                .contentType(MediaType.APPLICATION_JSON)
					                .content(testHelper.asJsonString(request)))
					.andDo(print())
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("존재하지 않는 사용자 차단 해제 시 404 에러")
		void unblockUser_WithNonExistentUser_ShouldReturn404() throws Exception {
			// Given
			var blocker = testHelper.createTestUser("blocker", Gender.MALE, 25, Region.SEOUL);
			var request = Map.of("blockedUserId", 99999L);

			// When & Then
			mockMvc.perform(testHelper.withAuth(delete("/api/v1/blocks"), blocker.accessToken())
					                .contentType(MediaType.APPLICATION_JSON)
					                .content(testHelper.asJsonString(request)))
					.andDo(print())
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("인증 없이 차단 해제 요청 시 401 에러")
		void unblockUser_WithoutAuth_ShouldReturn401() throws Exception {
			// Given
			var request = Map.of("blockedUserId", 1L);

			// When & Then
			mockMvc.perform(delete("/api/v1/blocks")
					                .contentType(MediaType.APPLICATION_JSON)
					                .content(testHelper.asJsonString(request)))
					.andDo(print())
					.andExpect(status().isUnauthorized());
		}
	}
}