package com.anonymouschat.anonymouschatserver.infra.security;

import com.anonymouschat.anonymouschatserver.common.exception.auth.UnsupportedAlgorithmAuthenticationException;
import com.anonymouschat.anonymouschatserver.domain.type.Role;
import com.anonymouschat.anonymouschatserver.infra.config.SecurityConfig;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtAuthenticationFactory;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtTokenResolver;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtValidator;
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

/**
 * Web 슬라이스 테스트 (보안 필터/핸들러 집중 검증)
 * - 기존 셋업/임포트 유지
 * - permitAll 엔드포인트(/actuator/health) 추가
 */
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
	@MockitoBean JwtValidator jwtValidator;
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
		doNothing().when(jwtValidator).validate(token);
		when(authFactory.createPrincipal(token))
				.thenReturn(new com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal(123L, null, null, Role.USER));
		when(authFactory.createAuthentication(any())).thenReturn(authentication);
	}

	// ---------- tests: UNAUTHORIZED(401) ----------
	@Test
	@DisplayName("보호 경로: 토큰 없음 → 401 UNAUTHORIZED (EntryPoint)")
	void protected_without_token_should_401() throws Exception {
		stubResolveNoToken();
		mockMvc.perform(get("/secure/user"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
	}

	@Test
	@DisplayName("만료 토큰 → 401 EXPIRED_TOKEN")
	void expired_token_should_401() throws Exception {
		String token = "expired";
		stubResolveToken(token);
		doThrow(new CredentialsExpiredException(EXPIRED_TOKEN.getMessage()))
				.when(jwtValidator).validate(token);

		mockMvc.perform(get("/secure/user"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value(UNAUTHORIZED.getCode()))
				.andExpect(jsonPath("$.error.message").value(UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("Malformed → 401 MALFORMED_TOKEN")
	void malformed_token_should_401() throws Exception {
		String token = "malformed";
		stubResolveToken(token);
		doThrow(new BadCredentialsException(MALFORMED_TOKEN.getMessage(),
				new io.jsonwebtoken.MalformedJwtException("bad")))
				.when(jwtValidator).validate(token);

		mockMvc.perform(get("/secure/user"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value(UNAUTHORIZED.getCode()))
				.andExpect(jsonPath("$.error.message").value(UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("Unsupported → 401 UNSUPPORTED_TOKEN")
	void unsupported_token_should_401() throws Exception {
		String token = "unsupported";
		stubResolveToken(token);
		doThrow(new BadCredentialsException(UNSUPPORTED_TOKEN.getMessage(),
				new UnsupportedJwtException("unsupported")))
            .when(jwtValidator).validate(token);


		mockMvc.perform(get("/secure/user"))
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
				.when(jwtValidator).validate(token);

		mockMvc.perform(get("/secure/user"))
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
				.when(jwtValidator).validate(token);

		mockMvc.perform(get("/secure/user"))
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
				.when(jwtValidator).validate(token);

		mockMvc.perform(get("/secure/user"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value(UNAUTHORIZED.getCode()))
				.andExpect(jsonPath("$.error.message").value(UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("필터 처리 중 예기치 못한 예외 → 401 UNAUTHORIZED")
	void unexpected_exception_should_401_unauthorized() throws Exception {
		String token = "boom";
		stubResolveToken(token);
		doThrow(new RuntimeException("boom")).when(jwtValidator).validate(token);

		mockMvc.perform(get("/secure/user"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value(UNAUTHORIZED.getCode()))
				.andExpect(jsonPath("$.error.message").value(UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("Authorization: Basic ... → 토큰 미존재와 동일하게 401")
	void non_bearer_scheme_should_401() throws Exception {
		// JwtTokenResolver는 Bearer만 추출 → 여기서는 토큰 미검출로 처리
		when(tokenResolver.resolve(any(HttpServletRequest.class))).thenReturn(null);

		mockMvc.perform(get("/secure/user").header("Authorization", "Basic abcdef"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value(UNAUTHORIZED.getCode()))
				.andExpect(jsonPath("$.error.message").value(UNAUTHORIZED.getMessage()));
	}

	@Test
	@DisplayName("Authentication 생성 실패(null) → 401")
	void authentication_factory_returns_null_should_401() throws Exception {
		String token = "ok-but-null-auth";
		stubResolveToken(token);
		doNothing().when(jwtValidator).validate(token);
		when(authFactory.createPrincipal(token))
				.thenReturn(new com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal(123L, null, null, Role.USER));
		when(authFactory.createAuthentication(any())).thenReturn(null); // 핵심

		mockMvc.perform(get("/secure/user"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value(UNAUTHORIZED.getCode()))
				.andExpect(jsonPath("$.error.message").value(UNAUTHORIZED.getMessage()));
	}

	// ---------- tests: FORBIDDEN(403) ----------
	@Test
	@DisplayName("인증은 됐지만 권한 부족 → 403 FORBIDDEN (AccessDeniedHandler)")
	void authenticated_but_forbidden_should_403() throws Exception {
		String token = "ok";
		var auth = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
				"user-123", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		mockAuthenticatedUser(token, auth);

		mockMvc.perform(get("/secure/admin"))
				.andExpect(status().isForbidden())
				.andExpect(content().contentTypeCompatibleWith("application/json"))
				.andExpect(jsonPath("$.error.code").value(FORBIDDEN.getCode()))
				.andExpect(jsonPath("$.error.message").value(FORBIDDEN.getMessage()));
	}

	// ---------- tests: OK(200) ----------
	@Test
	@DisplayName("ROLE_ADMIN 보유 시 /secure/admin → 200")
	void admin_with_admin_role_should_200() throws Exception {
		String token = "admin-ok";
		var auth = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
				"admin-1", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
		);
		mockAuthenticatedUser(token, auth);

		mockMvc.perform(get("/secure/admin"))
				.andExpect(status().isOk())
				.andExpect(content().string("admin ok"));
	}

	@Test
	@DisplayName("permitAll 경로(/actuator/health): 토큰 없이 200")
	void permit_all_endpoint_should_200_without_token() throws Exception {
		// 토큰 아예 안 줌(또는 아래처럼 명시적으로 null 스텁)
		stubResolveNoToken();

		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(content().string("ok"));
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
	@EnableMethodSecurity() // @PreAuthorize 활성화
	static class MethodSecurityTestConfig {}

	@Configuration
	static class SecurityTestStubs {
		// oauth2Login 의존 스텁
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

	// ====== 테스트 전용 컨트롤러 ======
	@RestController
	public static class TestSecurityController {
		// 보호 엔드포인트
		@GetMapping("/secure/user")
		public ResponseEntity<String> user() { return ResponseEntity.ok("user ok"); }

		// ROLE_ADMIN 필요
		@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
		@GetMapping("/secure/admin")
		public ResponseEntity<String> admin() { return ResponseEntity.ok("admin ok"); }

		// SecurityConfig 에서 permitAll로 열어둔 경로 중 하나
		@GetMapping("/actuator/health")
		public ResponseEntity<String> health() { return ResponseEntity.ok("ok"); }
	}
}
