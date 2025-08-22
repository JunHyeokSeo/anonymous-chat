package com.anonymouschat.anonymouschatserver.infra.security;

import com.anonymouschat.anonymouschatserver.application.service.AuthService;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;
import com.anonymouschat.anonymouschatserver.common.exception.auth.UnsupportedAlgorithmAuthenticationException;
import com.anonymouschat.anonymouschatserver.domain.type.Role;
import com.anonymouschat.anonymouschatserver.infra.config.SecurityConfig;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtAuthenticationFactory;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtTokenResolver;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.anonymouschat.anonymouschatserver.common.code.ErrorCode.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Import({
		SecurityConfig.class,
		SecurityExceptionHandlingWebTest.MethodSecurityTestConfig.class,
		SecurityExceptionHandlingWebTest.SecurityTestStubs.class,
		SecurityExceptionHandlingWebTest.TestMvcControllers.class
})
class SecurityExceptionHandlingWebTest {

	@Resource MockMvc mockMvc;

	@MockitoBean OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
	@MockitoBean JwtTokenResolver tokenResolver;
	@MockitoBean AuthService authService;
	@MockitoBean JwtAuthenticationFactory authFactory;

	// ---------- helpers ----------
	private void stubResolveNoToken() {
		when(tokenResolver.resolve(any(HttpServletRequest.class))).thenReturn(null);
	}

	private void stubResolveToken(String token) {
		when(tokenResolver.resolve(any(HttpServletRequest.class))).thenReturn(token);
	}

	private void mockAuthenticatedUser(String token, Authentication authentication) {
		stubResolveToken(token);
		doNothing().when(authService).validateToken(token);
		when(authFactory.createPrincipal(token))
				.thenReturn(CustomPrincipal.builder()
						            .userId(123L)
						            .role(Role.USER)
						            .build());
		when(authFactory.createAuthentication(any())).thenReturn(authentication);
	}

	// ---------- tests: UNAUTHORIZED(401) ----------
	@Test
	@DisplayName("보호 경로: 토큰 없음 → 401 UNAUTHORIZED")
	void protected_without_token_should_401() throws Exception {
		stubResolveNoToken();

		mockMvc.perform(get("/api/secure/user"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
	}

	@Test
	@DisplayName("만료 토큰 → 401 EXPIRED_TOKEN")
	void expired_token_should_401() throws Exception {
		String token = "expired";
		stubResolveToken(token);
		doThrow(new CredentialsExpiredException(EXPIRED_TOKEN.getMessage()))
				.when(authService).validateToken(token);

		mockMvc.perform(get("/api/secure/user"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value(UNAUTHORIZED.getCode()))
				.andExpect(jsonPath("$.error.message").value(UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("블랙리스트된 토큰 → 401 INVALID_TOKEN")
	void blacklisted_token_should_401() throws Exception {
		String token = "blacklisted";
		stubResolveToken(token);
		doThrow(new InvalidTokenException(INVALID_TOKEN))
				.when(authService).validateToken(token);

		mockMvc.perform(get("/api/secure/user"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value(UNAUTHORIZED.getCode()))
				.andExpect(jsonPath("$.error.message").value(UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("Malformed JWT → 401 MALFORMED_TOKEN")
	void malformed_token_should_401() throws Exception {
		String token = "malformed";
		stubResolveToken(token);
		doThrow(new BadCredentialsException(MALFORMED_TOKEN.getMessage(),
				new io.jsonwebtoken.MalformedJwtException("bad")))
				.when(authService).validateToken(token);

		mockMvc.perform(get("/api/secure/user"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value(UNAUTHORIZED.getCode()))
				.andExpect(jsonPath("$.error.message").value(UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("Unsupported JWT → 401 UNSUPPORTED_TOKEN")
	void unsupported_token_should_401() throws Exception {
		String token = "unsupported";
		stubResolveToken(token);
		doThrow(new BadCredentialsException(UNSUPPORTED_TOKEN.getMessage(),
				new UnsupportedJwtException("unsupported")))
				.when(authService).validateToken(token);

		mockMvc.perform(get("/api/secure/user"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value(UNAUTHORIZED.getCode()))
				.andExpect(jsonPath("$.error.message").value(UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("서명 불일치 → 401 INVALID_SIGNATURE")
	void invalid_signature_should_401() throws Exception {
		String token = "bad-signature";
		stubResolveToken(token);
		doThrow(new BadCredentialsException(INVALID_SIGNATURE.getMessage(),
				new SecurityException("sig invalid")))
				.when(authService).validateToken(token);

		mockMvc.perform(get("/api/secure/user"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value(UNAUTHORIZED.getCode()))
				.andExpect(jsonPath("$.error.message").value(UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("기타 BadCredentials → 401 INVALID_TOKEN")
	void invalid_token_generic_should_401() throws Exception {
		String token = "invalid";
		stubResolveToken(token);
		doThrow(new BadCredentialsException(INVALID_TOKEN.getMessage()))
				.when(authService).validateToken(token);

		mockMvc.perform(get("/api/secure/user"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value(UNAUTHORIZED.getCode()))
				.andExpect(jsonPath("$.error.message").value(UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("알고리즘 불일치 → 401 UNSUPPORTED_ALGORITHM")
	void unsupported_algorithm_should_401() throws Exception {
		String token = "alg-mismatch";
		stubResolveToken(token);
		doThrow(new UnsupportedAlgorithmAuthenticationException(UNSUPPORTED_ALGORITHM.getMessage()))
				.when(authService).validateToken(token);

		mockMvc.perform(get("/api/secure/user"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value(UNAUTHORIZED.getCode()))
				.andExpect(jsonPath("$.error.message").value(UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("Principal 생성 실패 → 401")
	void principal_creation_failed_should_401() throws Exception {
		String token = "principal-fail";
		stubResolveToken(token);
		doNothing().when(authService).validateToken(token);
		when(authFactory.createPrincipal(token))
				.thenThrow(new RuntimeException("Principal creation failed"));

		mockMvc.perform(get("/api/secure/user"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value(UNAUTHORIZED.getCode()))
				.andExpect(jsonPath("$.error.message").value(UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("Authentication 생성 실패(null) → 401")
	void authentication_factory_returns_null_should_401() throws Exception {
		String token = "ok-but-null-auth";
		stubResolveToken(token);
		doNothing().when(authService).validateToken(token);
		when(authFactory.createPrincipal(token))
				.thenReturn(CustomPrincipal.builder()
						            .userId(123L)
						            .role(Role.USER)
						            .build());
		when(authFactory.createAuthentication(any())).thenReturn(null);

		mockMvc.perform(get("/api/secure/user"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value(UNAUTHORIZED.getCode()))
				.andExpect(jsonPath("$.error.message").value(UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("Authorization: Basic ... → 토큰 미존재와 동일하게 401")
	void non_bearer_scheme_should_401() throws Exception {
		when(tokenResolver.resolve(any(HttpServletRequest.class))).thenReturn(null);

		mockMvc.perform(get("/api/secure/user").header("Authorization", "Basic abcdef"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value(UNAUTHORIZED.getCode()))
				.andExpect(jsonPath("$.error.message").value(UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("필터 처리 중 예기치 못한 예외 → 401 UNAUTHORIZED")
	void unexpected_exception_should_401_unauthorized() throws Exception {
		String token = "boom";
		stubResolveToken(token);
		doThrow(new RuntimeException("boom")).when(authService).validateToken(token);

		mockMvc.perform(get("/api/secure/user"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value(UNAUTHORIZED.getCode()))
				.andExpect(jsonPath("$.error.message").value(UNAUTHORIZED.getMessage()));
	}

	// ---------- tests: FORBIDDEN(403) ----------
	@Test
	@DisplayName("인증은 됐지만 권한 부족 → 403 FORBIDDEN")
	void authenticated_but_forbidden_should_403() throws Exception {
		String token = "ok";
		var auth = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
				"user-123", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		mockAuthenticatedUser(token, auth);

		mockMvc.perform(get("/api/secure/admin"))
				.andExpect(status().isForbidden())
				.andExpect(content().contentTypeCompatibleWith("application/json"))
				.andExpect(jsonPath("$.error.code").value(FORBIDDEN.getCode()))
				.andExpect(jsonPath("$.error.message").value(FORBIDDEN.getMessage()));
	}

	// ---------- tests: OK(200) ----------
	@Test
	@DisplayName("ROLE_USER 권한으로 /api/secure/user → 200")
	void user_with_user_role_should_200() throws Exception {
		String token = "user-ok";
		var auth = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
				"user-123", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		mockAuthenticatedUser(token, auth);

		mockMvc.perform(get("/api/secure/user"))
				.andExpect(status().isOk())
				.andExpect(content().string("user ok"));
	}

	@Test
	@DisplayName("ROLE_ADMIN 권한으로 /api/secure/admin → 200")
	void admin_with_admin_role_should_200() throws Exception {
		String token = "admin-ok";
		var auth = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
				"admin-1", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
		);
		mockAuthenticatedUser(token, auth);

		mockMvc.perform(get("/api/secure/admin"))
				.andExpect(status().isOk())
				.andExpect(content().string("admin ok"));
	}

	@Test
	@DisplayName("permitAll 경로(/actuator/health): 토큰 없이 200")
	void permit_all_endpoint_should_200_without_token() throws Exception {
		stubResolveNoToken();

		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(content().string("ok"));
	}

	@Test
	@DisplayName("GUEST 역할도 USER 엔드포인트 접근 가능")
	void guest_can_access_user_endpoint() throws Exception {
		String token = "guest-ok";
		var auth = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
				"guest-123", null, List.of(new SimpleGrantedAuthority("ROLE_GUEST"))
		);

		stubResolveToken(token);
		doNothing().when(authService).validateToken(token);
		when(authFactory.createPrincipal(token))
				.thenReturn(CustomPrincipal.builder()
						            .userId(123L)
						            .role(Role.GUEST)
						            .build());
		when(authFactory.createAuthentication(any())).thenReturn(auth);

		mockMvc.perform(get("/api/secure/user"))
				.andExpect(status().isOk())
				.andExpect(content().string("user ok"));
	}

	// ========= 테스트 전용 빈들 =========
	@Configuration
	static class TestMvcControllers {
		@Bean
		TestSecurityController testSecurityController() {
			return new TestSecurityController();
		}
	}

	@Configuration
	@EnableMethodSecurity
	static class MethodSecurityTestConfig {}

	@Configuration
	static class SecurityTestStubs {
		@Bean
		org.springframework.security.oauth2.client.registration.ClientRegistrationRepository clientRegistrationRepository() {
			return id -> null;
		}

		@Bean
		org.springframework.security.oauth2.client.OAuth2AuthorizedClientService authorizedClientService(
				org.springframework.security.oauth2.client.registration.ClientRegistrationRepository repo) {
			return new org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService(repo);
		}
	}

	@RestController
	public static class TestSecurityController {
		@GetMapping("/api/secure/user")
		public ResponseEntity<String> user() {
			return ResponseEntity.ok("user ok");
		}

		@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
		@GetMapping("/api/secure/admin")
		public ResponseEntity<String> admin() {
			return ResponseEntity.ok("admin ok");
		}

		@GetMapping("/actuator/health")
		public ResponseEntity<String> health() {
			return ResponseEntity.ok("ok");
		}
	}
}