package com.anonymouschat.anonymouschatserver.integration.controller;

import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import com.anonymouschat.anonymouschatserver.integration.common.IntegrationTestHelper;
import com.anonymouschat.anonymouschatserver.integration.config.TestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestConfig.class)
public class ChatRoomControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private IntegrationTestHelper testHelper;

	@AfterEach
	void cleanup() {
		testHelper.cleanup();
	}

	@Nested
	@DisplayName("채팅방 생성/조회 API")
	class CreateOrFindChatRoomTest {

		@Test
		@DisplayName("새로운 채팅방 생성 성공")
		void createOrFind_WithValidRecipient_ShouldCreateChatRoom() throws Exception {
			// Given
			var user1 = testHelper.createTestUser("user1", Gender.MALE, 25, Region.SEOUL);
			var user2 = testHelper.createTestUser("user2", Gender.FEMALE, 27, Region.BUSAN);

			var request = Map.of("recipientId", user2.user().getId());

			// When & Then
			mockMvc.perform(testHelper.withAuth(post("/api/v1/chat-rooms"), user1.accessToken())
					                .contentType(MediaType.APPLICATION_JSON)
					                .content(testHelper.asJsonString(request)))
					.andDo(print())
					.andExpect(status().isCreated())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.roomId").exists())
					.andExpect(jsonPath("$.error").doesNotExist());
		}

		@Test
		@DisplayName("자기 자신과의 채팅방 생성 시 400 에러")
		void createOrFind_WithSelfRecipient_ShouldReturn400() throws Exception {
			// Given
			var user1 = testHelper.createTestUser("user1", Gender.MALE, 25, Region.SEOUL);
			var request = Map.of("recipientId", user1.user().getId());

			// When & Then
			mockMvc.perform(testHelper.withAuth(post("/api/v1/chat-rooms"), user1.accessToken())
					                .contentType(MediaType.APPLICATION_JSON)
					                .content(testHelper.asJsonString(request)))
					.andDo(print())
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("존재하지 않는 사용자와 채팅방 생성 시 404 에러")
		void createOrFind_WithNonExistentUser_ShouldReturn404() throws Exception {
			// Given
			var user1 = testHelper.createTestUser("user1", Gender.MALE, 25, Region.SEOUL);
			var request = Map.of("recipientId", 99999L);

			// When & Then
			mockMvc.perform(testHelper.withAuth(post("/api/v1/chat-rooms"), user1.accessToken())
					                .contentType(MediaType.APPLICATION_JSON)
					                .content(testHelper.asJsonString(request)))
					.andDo(print())
					.andExpect(status().isNotFound());
		}
	}

	@Nested
	@DisplayName("참여 중인 채팅방 목록 조회 API")
	class GetActiveChatRoomsTest {

		@Test
		@DisplayName("채팅방 목록 조회 성공")
		void getMyActiveChatRooms_WithValidAuth_ShouldReturnChatRooms() throws Exception {
			// Given
			var user1 = testHelper.createTestUser("user1", Gender.MALE, 25, Region.SEOUL);

			// When & Then
			mockMvc.perform(testHelper.withAuth(get("/api/v1/chat-rooms"), user1.accessToken()))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data").isArray())
					.andExpect(jsonPath("$.error").doesNotExist());
		}

		@Test
		@DisplayName("인증 없이 요청 시 401 에러")
		void getMyActiveChatRooms_WithoutAuth_ShouldReturn401() throws Exception {
			mockMvc.perform(get("/api/v1/chat-rooms"))
					.andDo(print())
					.andExpect(status().isUnauthorized());
		}
	}

	@Nested
	@DisplayName("채팅방 나가기 API")
	class ExitChatRoomTest {

		@Test
		@DisplayName("채팅방 나가기 성공")
		void exitChatRoom_WithValidRoomId_ShouldReturnNoContent() throws Exception {
			// Given
			var user1 = testHelper.createTestUser("user1", Gender.MALE, 25, Region.SEOUL);
			var user2 = testHelper.createTestUser("user2", Gender.FEMALE, 27, Region.BUSAN);

			// 먼저 채팅방 생성
			var createRequest = Map.of("recipientId", user2.user().getId());
			var result = mockMvc.perform(testHelper.withAuth(post("/api/v1/chat-rooms"), user1.accessToken())
					                             .contentType(MediaType.APPLICATION_JSON)
					                             .content(testHelper.asJsonString(createRequest)))
					             .andReturn();

			String responseBody = result.getResponse().getContentAsString();
			var jsonNode = new ObjectMapper().readTree(responseBody);
			Long roomId = jsonNode.get("data").get("roomId").asLong();

			// When & Then
			mockMvc.perform(testHelper.withAuth(delete("/api/v1/chat-rooms/{roomId}", roomId), user1.accessToken()))
					.andDo(print())
					.andExpect(status().isNoContent());
		}

		@Test
		@DisplayName("존재하지 않는 채팅방에서 나가기 시 404 에러")
		void exitChatRoom_WithNonExistentRoom_ShouldReturn404() throws Exception {
			// Given
			var user1 = testHelper.createTestUser("user1", Gender.MALE, 25, Region.SEOUL);

			// When & Then
			mockMvc.perform(testHelper.withAuth(delete("/api/v1/chat-rooms/{roomId}", 99999L), user1.accessToken()))
					.andDo(print())
					.andExpect(status().isNotFound());
		}
	}
}
