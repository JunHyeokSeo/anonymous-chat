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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestConfig.class)
class UserControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private IntegrationTestHelper testHelper;

	@AfterEach
	void cleanup() {
		testHelper.cleanup();
	}

	@Nested
	@DisplayName("회원가입 API")
	class RegisterTest {

		@Test
		@DisplayName("유효한 정보로 회원가입 성공")
		void register_WithValidData_ShouldReturnAccessToken() throws Exception {
			// Given
			var guestUser = testHelper.createGuestUser();
			var registerRequest = Map.of(
					"nickname", "newuser",
					"gender", "MALE",
					"age", 25,
					"region", "SEOUL",
					"bio", "Hello, I'm new user!"
			);

			var requestPart = new MockMultipartFile(
					"request",
					"",
					MediaType.APPLICATION_JSON_VALUE,
					testHelper.asJsonString(registerRequest).getBytes()
			);

			// When & Then
			mockMvc.perform(testHelper.withAuth(multipart("/api/v1/users")
					                                    .file(requestPart), guestUser.accessToken()))
					.andDo(print())
					.andExpect(status().isCreated())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.accessToken").exists())
					.andExpect(jsonPath("$.error").doesNotExist());
		}

		@Test
		@DisplayName("유효하지 않은 정보로 회원가입 시 400 에러")
		void register_WithInvalidData_ShouldReturn400() throws Exception {
			// Given
			var guestUser = testHelper.createGuestUser();
			var registerRequest = Map.of(
					"nickname", "", // 빈 닉네임
					"gender", "INVALID_GENDER",
					"age", -1, // 유효하지 않은 나이
					"region", "INVALID_REGION"
			);

			var requestPart = new MockMultipartFile(
					"request",
					"",
					MediaType.APPLICATION_JSON_VALUE,
					testHelper.asJsonString(registerRequest).getBytes()
			);

			// When & Then
			mockMvc.perform(testHelper.withAuth(multipart("/api/v1/users")
					                                    .file(requestPart), guestUser.accessToken()))
					.andDo(print())
					.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("프로필 조회 API")
	class GetProfileTest {

		@Test
		@DisplayName("내 프로필 조회 성공")
		void getMyProfile_WithValidAuth_ShouldReturnProfile() throws Exception {
			// Given
			var testUser = testHelper.createTestUser("testuser", Gender.MALE, 25, Region.SEOUL);

			// When & Then
			mockMvc.perform(testHelper.withAuth(get("/api/v1/users/me"), testUser.accessToken()))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.nickname").value("testuser"))
					.andExpect(jsonPath("$.data.gender").value("MALE"))
					.andExpect(jsonPath("$.data.age").value(25))
					.andExpect(jsonPath("$.error").doesNotExist());
		}

		@Test
		@DisplayName("다른 사용자 프로필 조회 성공")
		void getUserProfile_WithValidUserId_ShouldReturnProfile() throws Exception {
			// Given
			var user1 = testHelper.createTestUser("user1", Gender.MALE, 25, Region.SEOUL);
			var user2 = testHelper.createTestUser("user2", Gender.FEMALE, 27, Region.BUSAN);

			// When & Then
			mockMvc.perform(testHelper.withAuth(get("/api/v1/users/{userId}", user2.user().getId()), user1.accessToken()))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.nickname").value("user2"))
					.andExpect(jsonPath("$.error").doesNotExist());
		}
	}

	@Nested
	@DisplayName("사용자 검색 API")
	class SearchUsersTest {

		@Test
		@DisplayName("필터 조건으로 사용자 검색 성공")
		void getUserList_WithFilters_ShouldReturnFilteredUsers() throws Exception {
			// Given
			var searchUser = testHelper.createTestUser("searcher", Gender.MALE, 30, Region.SEOUL);
			testHelper.createTestUser("male25", Gender.MALE, 25, Region.SEOUL);
			testHelper.createTestUser("female30", Gender.FEMALE, 30, Region.BUSAN);

			// When & Then
			mockMvc.perform(testHelper.withAuth(get("/api/v1/users")
					                                    .param("gender", "MALE")
					                                    .param("minAge", "20")
					                                    .param("maxAge", "30")
					                                    .param("region", "SEOUL"), searchUser.accessToken()))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.content").isArray())
					.andExpect(jsonPath("$.error").doesNotExist());
		}
	}

	@Nested
	@DisplayName("프로필 수정 API")
	class UpdateProfileTest {

		@Test
		@DisplayName("프로필 수정 성공")
		void updateProfile_WithValidData_ShouldReturnSuccess() throws Exception {
			// Given
			var testUser = testHelper.createTestUser("testuser", Gender.MALE, 25, Region.SEOUL);
			var updateRequest = Map.of(
					"nickname", "updateduser",
					"gender", "MALE",
					"age", 26,
					"region", "BUSAN",
					"bio", "Updated bio"
			);

			var requestPart = new MockMultipartFile(
					"request",
					"",
					MediaType.APPLICATION_JSON_VALUE,
					testHelper.asJsonString(updateRequest).getBytes()
			);

			// When & Then
			mockMvc.perform(testHelper.withAuth(multipart("/api/v1/users/me")
					                                    .file(requestPart)
					                                    .with(request -> {
						                                    request.setMethod("PUT");
						                                    return request;
					                                    }), testUser.accessToken()))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.error").doesNotExist());
		}
	}

	@Nested
	@DisplayName("회원 탈퇴 API")
	class WithdrawTest {

		@Test
		@DisplayName("회원 탈퇴 성공")
		void withdraw_WithValidAuth_ShouldReturnSuccess() throws Exception {
			// Given
			var testUser = testHelper.createTestUser("testuser", Gender.MALE, 25, Region.SEOUL);

			// When & Then
			mockMvc.perform(testHelper.withAuth(delete("/api/v1/users/me"), testUser.accessToken()))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.error").doesNotExist());
		}
	}
}
