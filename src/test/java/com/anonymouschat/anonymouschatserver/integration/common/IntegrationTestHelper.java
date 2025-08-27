package com.anonymouschat.anonymouschatserver.integration.common;

import com.anonymouschat.anonymouschatserver.application.dto.MessageUseCaseDto;
import com.anonymouschat.anonymouschatserver.application.service.AuthService;
import com.anonymouschat.anonymouschatserver.application.service.ChatRoomService;
import com.anonymouschat.anonymouschatserver.application.usecase.MessageUseCase;
import com.anonymouschat.anonymouschatserver.domain.entity.User;
import com.anonymouschat.anonymouschatserver.domain.repository.UserRepository;
import com.anonymouschat.anonymouschatserver.domain.type.Gender;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Region;
import com.anonymouschat.anonymouschatserver.domain.type.Role;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@Component
public class IntegrationTestHelper {
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final ObjectMapper objectMapper;
	private final AuthService authService;
	private final ChatRoomService chatRoomService;
	private final MessageUseCase messageUseCase;

	public IntegrationTestHelper(UserRepository userRepository,
	                             JwtTokenProvider jwtTokenProvider,
	                             ObjectMapper objectMapper,
	                             AuthService authService,
	                             ChatRoomService chatRoomService,
	                             MessageUseCase messageUseCase) {
		this.userRepository = userRepository;
		this.jwtTokenProvider = jwtTokenProvider;
		this.objectMapper = objectMapper;
		this.authService = authService;
		this.chatRoomService = chatRoomService;
		this.messageUseCase = messageUseCase;
	}

	/**
	 * 테스트용 사용자 생성
	 */
	public TestUser createTestUser(String nickname, Gender gender, int age, Region region) {
		User user = User.builder()
				            .provider(OAuthProvider.GOOGLE)
				            .providerId("test_provider_id_" + nickname)
				            .role(Role.USER)
				            .nickname(nickname)
				            .gender(gender)
				            .age(age)
				            .region(region)
				            .bio("Test user bio for " + nickname)
				            .build();

		User savedUser = userRepository.save(user);
		String accessToken = jwtTokenProvider.createAccessToken(savedUser.getId(), savedUser.getRole());
		String refreshToken = jwtTokenProvider.createRefreshToken(savedUser.getId(), savedUser.getRole());

		// AuthService 사용하여 Redis에 저장
		authService.saveRefreshToken(savedUser.getId(), refreshToken, "test-agent", "127.0.0.1");

		return new TestUser(savedUser, accessToken, refreshToken);
	}

	/**
	 * GUEST 권한 테스트 사용자 생성 (회원가입용)
	 */
	public TestUser createGuestUser() {
		User user = User.builder()
				            .provider(OAuthProvider.GOOGLE)
				            .providerId("guest_provider_id")
				            .nickname("guestUser")
				            .role(Role.GUEST)
				            .gender(Gender.UNKNOWN)
				            .region(Region.UNKNOWN)
				            .build();

		User savedUser = userRepository.save(user);
		String accessToken = jwtTokenProvider.createAccessToken(savedUser.getId(), savedUser.getRole());

		return new TestUser(savedUser, accessToken, null);
	}

	/**
	 * Authorization 헤더 추가
	 */
	public MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder builder, String accessToken) {
		return builder.header("Authorization", "Bearer " + accessToken);
	}

	/**
	 * JSON 요청 본문 생성
	 */
	public String asJsonString(Object obj) throws Exception {
		return objectMapper.writeValueAsString(obj);
	}

	/**
	 * 테스트 데이터 정리
	 */
	public void cleanup() {
		userRepository.deleteAll();
	}

	public Long createChatRoom(User user1, User user2) {
		return chatRoomService.createOrFind(user1, user2).getId();
	}

	public Long createMessage(Long roomId, Long userId, String content){
		 return messageUseCase.sendMessage(MessageUseCaseDto.SendMessageRequest.builder()
				                                    .roomId(roomId)
				                                    .senderId(userId)
				                                    .content(content)
				                                    .build());
	}

	public void markMessageAsRead(Long roomId, Long userId) {
		messageUseCase.markMessagesAsRead(MessageUseCaseDto.MarkMessagesAsReadRequest.builder()
				                                  .roomId(roomId)
				                                  .userId(userId)
				                                  .build());
	}

	public record TestUser(User user, String accessToken, String refreshToken) {}
}