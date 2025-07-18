package com.anonymouschat.anonymouschatserver.web.api.auth;

import com.anonymouschat.anonymouschatserver.global.testsupport.AbstractIntegrationTest;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.LoginRequest;
import com.anonymouschat.anonymouschatserver.web.api.auth.dto.RefreshTokenRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerTest extends AbstractIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@Transactional
	void 로그인_요청시_토큰을_반환한다() throws Exception {
		// given
		LoginRequest request = new LoginRequest("test-user");

		// when & then
		mockMvc.perform(post("/api/v1/auth/login")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.accessToken").exists())
				.andExpect(jsonPath("$.data.refreshToken").exists());
	}

	@Test
	@Transactional
	void 리프레시_토큰으로_액세스토큰_재발급() throws Exception {
		// given
		LoginRequest request = new LoginRequest("test-user2");
		String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
				                                       .contentType(MediaType.APPLICATION_JSON)
				                                       .content(objectMapper.writeValueAsString(request)))
				                       .andExpect(status().isOk())
				                       .andReturn()
				                       .getResponse()
				                       .getContentAsString();

		String refreshToken = JsonPath.read(loginResponse, "$.data.refreshToken");

		RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

		// when & then
		mockMvc.perform(post("/api/v1/auth/refresh")
				                .contentType(MediaType.APPLICATION_JSON)
				                .content(objectMapper.writeValueAsString(refreshRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.accessToken").exists());
	}

	@Test
	@Transactional
	void 로그아웃_요청시_리프레시_토큰_삭제된다() throws Exception {
		// given
		LoginRequest request = new LoginRequest("logout-user");
		String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
				                                       .contentType(MediaType.APPLICATION_JSON)
				                                       .content(objectMapper.writeValueAsString(request)))
				                       .andReturn()
				                       .getResponse()
				                       .getContentAsString();

		String accessToken = JsonPath.read(loginResponse, "$.data.accessToken");

		// when
		mockMvc.perform(post("/api/v1/auth/logout")
				                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
				.andExpect(status().isOk());
	}
}