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
public class AuthCallbackControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private IntegrationTestHelper testHelper;

	@AfterEach
	void cleanup() {
		testHelper.cleanup();
	}

	@Nested
	@DisplayName("OAuth 콜백 처리 API")
	class AuthCallbackTest {

		@Test
		@DisplayName("유효한 코드로 콜백 처리 성공")
		void authCallback_WithValidCode_ShouldReturnAuthCallbackPage() throws Exception {
			// Given
			var testUser = testHelper.createTestUser("callbackUser", Gender.MALE, 25, Region.SEOUL);
			String tempCode = testHelper.createAndStoreOAuthTempCode(testUser.user());

			// When & Then
			mockMvc.perform(get("/auth/callback").param("code", tempCode))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(view().name("pages/auth-callback"))
					.andExpect(model().attributeExists("authData"));
		}

		@Test
		@DisplayName("유효하지 않은 코드로 콜백 처리 시 리다이렉트")
		void authCallback_WithInvalidCode_ShouldRedirectToLogin() throws Exception {
			// Given
			String invalidCode = "invalid_temp_code";

			// When & Then
			mockMvc.perform(get("/auth/callback")
					                .param("code", invalidCode))
					.andDo(print())
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/login?error=invalid_code"));
		}

		@Test
		@DisplayName("만료된 코드로 콜백 처리 시 리다이렉트")
		void authCallback_WithExpiredCode_ShouldRedirectToLogin() throws Exception {
			// Given
			String expiredCode = "expired_temp_code";

			// When & Then
			mockMvc.perform(get("/auth/callback")
					                .param("code", expiredCode))
					.andDo(print())
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/login?error=invalid_code"));
		}

		@Test
		@DisplayName("코드 파라미터 없이 요청 시 400 에러")
		void authCallback_WithoutCodeParameter_ShouldReturn400() throws Exception {
			mockMvc.perform(get("/auth/callback"))
					.andDo(print())
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("서버 오류 발생 시 에러 리다이렉트")
		void authCallback_WithServerError_ShouldRedirectToLoginWithError() throws Exception {
			// Given - 서버 오류를 유발하는 코드 (구현에 따라 다름)
			String errorCode = "server_error_code";

			// When & Then
			mockMvc.perform(get("/auth/callback")
					                .param("code", errorCode))
					.andDo(print())
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrlPattern("/login?error=*"));
		}
	}
}