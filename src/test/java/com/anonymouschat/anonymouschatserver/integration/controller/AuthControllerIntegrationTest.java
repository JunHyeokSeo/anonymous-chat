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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestConfig.class)
class AuthControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private IntegrationTestHelper testHelper;

	@AfterEach
	void cleanup() {
		testHelper.cleanup();
	}

	@Nested
	@DisplayName("토큰 재발급 API")
	class RefreshTokenTest {

		@Test
		@DisplayName("유효한 토큰으로 재발급 성공")
		void refresh_WithValidToken_ShouldReturnNewAccessToken() throws Exception {
			// Given
			var testUser = testHelper.createTestUser("testuser", Gender.MALE, 25, Region.SEOUL);

			// When & Then
			mockMvc.perform(testHelper.withAuth(post("/api/v1/auth/refresh"), testUser.accessToken())
					                .contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.accessToken").exists());
		}

		@Test
		@DisplayName("인증 없이 요청 시 401 에러")
		void refresh_WithoutAuth_ShouldReturn401() throws Exception {
			mockMvc.perform(post("/api/v1/auth/refresh")
					                .contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("유효하지 않은 토큰으로 요청 시 401 에러")
		void refresh_WithInvalidToken_ShouldReturn401() throws Exception {
			mockMvc.perform(post("/api/v1/auth/refresh")
					                .header("Authorization", "Bearer invalid_token")
					                .contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isUnauthorized());
		}
	}

	@Nested
	@DisplayName("로그아웃 API")
	class LogoutTest {

		@Test
		@DisplayName("유효한 토큰으로 로그아웃 성공")
		void logout_WithValidToken_ShouldReturnSuccess() throws Exception {
			// Given
			var testUser = testHelper.createTestUser("testuser", Gender.FEMALE, 30, Region.BUSAN);

			// When & Then
			mockMvc.perform(testHelper.withAuth(post("/api/v1/auth/logout"), testUser.accessToken())
					                .contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$.success").value(true));
		}

		@Test
		@DisplayName("인증 없이 로그아웃 요청 시 401 에러")
		void logout_WithoutAuth_ShouldReturn401() throws Exception {
			mockMvc.perform(post("/api/v1/auth/logout")
					                .contentType(MediaType.APPLICATION_JSON))
					.andDo(print())
					.andExpect(status().isUnauthorized());
		}
	}
}