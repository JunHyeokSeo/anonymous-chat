package com.anonymouschat.anonymouschatserver.example;

import com.anonymouschat.anonymouschatserver.domain.type.UserRole;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtTokenProvider;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestController.class)
@ActiveProfiles("test")
class JwtAuthenticationIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Test
	void 인증된_사용자는_ROLE_USER_권한으로_요청할_수_있다() throws Exception {
		// given
		Long userId = 1L;
		String accessToken = jwtTokenProvider.createAccessToken(userId, UserRole.ROLE_USER.getAuthority());

		// when & then
		mockMvc.perform(get("/api/v1/test/me")
				                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(content().string("Hello, userId = " + userId));
	}

	@Test
	void 토큰이_없는_요청은_401을_반환한다() throws Exception {
		mockMvc.perform(get("/api/v1/test/me"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void userId가_없는_토큰은_SecurityContext에_주입되지_않는다() throws Exception {
		// given: 가입 전 사용자의 토큰
		String token = jwtTokenProvider.createAccessTokenForOAuthLogin(OAuthProvider.GOOGLE, "some-google-id");

		mockMvc.perform(get("/api/v1/test/me")
				                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isUnauthorized()); // @PreAuthorize 통과 못 함
	}
}
