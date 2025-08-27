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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestConfig.class)
class MessageControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private IntegrationTestHelper testHelper;

	@AfterEach
	void cleanup() {
		testHelper.cleanup();
	}

	@Nested
	@DisplayName("메시지 조회 API")
	class GetMessagesTest {

		@Test
		@DisplayName("채팅방 메시지 조회 성공")
		void getMessages_WithValidRoomId_ShouldReturnMessages() throws Exception {
			// Given
			var testUser1 = testHelper.createTestUser("testuser1", Gender.MALE, 25, Region.SEOUL);
			var testUser2 = testHelper.createTestUser("testuser2", Gender.FEMALE, 27, Region.BUSAN);
			Long roomId = testHelper.createChatRoom(testUser1.user(), testUser2.user());

			// When & Then
			mockMvc.perform(testHelper.withAuth(get("/api/v1/messages")
					                                    .param("roomId", roomId.toString())
					                                    .param("limit", "20"), testUser1.accessToken()))
					.andExpect(status().isOk());
		}


		@Test
		@DisplayName("유효하지 않은 roomId로 조회 시 400 에러")
		void getMessages_WithInvalidRoomId_ShouldReturn400() throws Exception {
			// Given
			var testUser = testHelper.createTestUser("testuser", Gender.MALE, 25, Region.SEOUL);

			// When & Then
			mockMvc.perform(testHelper.withAuth(get("/api/v1/messages")
					                                    .param("roomId", "invalid"), testUser.accessToken()))
					.andDo(print())
					.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("메시지 읽음 처리 API")
	class MarkMessagesAsReadTest {

		@Test
		@DisplayName("메시지 읽음 처리 성공")
		void markMessagesAsRead_WithValidRoomId_ShouldReturnUpdatedCount() throws Exception {
			// Given
			var testUser1 = testHelper.createTestUser("testuser1", Gender.MALE, 25, Region.SEOUL);
			var testUser2 = testHelper.createTestUser("testuser2", Gender.FEMALE, 27, Region.BUSAN);
			Long roomId = testHelper.createChatRoom(testUser1.user(), testUser2.user());

			var request = Map.of("roomId", roomId); // 실제 roomId 사용

			// When & Then
			mockMvc.perform(testHelper.withAuth(post("/api/v1/messages/read"), testUser1.accessToken())
					                .contentType(MediaType.APPLICATION_JSON)
					                .content(testHelper.asJsonString(request)))
					.andExpect(status().isOk());
		}
	}

	@Nested
	@DisplayName("마지막 읽은 메시지 조회 API")
	class GetLastReadMessageTest {

		@Test
		@DisplayName("마지막 읽은 메시지 ID 조회 성공")
		void getLastReadMessageIdByOpponent_WithValidRoomId_ShouldReturnMessageId() throws Exception {
			// Given
			var testUser1 = testHelper.createTestUser("testuser1", Gender.MALE, 25, Region.SEOUL);
			var testUser2 = testHelper.createTestUser("testuser2", Gender.MALE, 25, Region.SEOUL);
			Long roomId = testHelper.createChatRoom(testUser1.user(), testUser2.user());
			testHelper.createMessage(roomId, testUser2.user().getId(), "test message");
			testHelper.markMessageAsRead(roomId, testUser1.user().getId());

			// When & Then
			mockMvc.perform(testHelper.withAuth(get("/api/v1/messages/last-read")
					                                    .param("roomId", roomId.toString()), testUser2.accessToken()))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data").isNumber())
					.andExpect(jsonPath("$.error").doesNotExist());
		}

		@Test
		@DisplayName("존재하지 않는 채팅방 조회 시 404 에러")
		void getLastReadMessageIdByOpponent_WithNonExistentRoom_ShouldReturn404() throws Exception {
			// Given
			var testUser = testHelper.createTestUser("testuser", Gender.MALE, 25, Region.SEOUL);

			// When & Then
			mockMvc.perform(testHelper.withAuth(get("/api/v1/messages/last-read")
					                                    .param("roomId", "99999"), testUser.accessToken()))
					.andDo(print())
					.andExpect(status().isNotFound());
		}
	}
}