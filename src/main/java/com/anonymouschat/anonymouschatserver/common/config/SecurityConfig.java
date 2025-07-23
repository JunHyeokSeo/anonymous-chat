package com.anonymouschat.anonymouschatserver.common.config;

import com.anonymouschat.anonymouschatserver.common.jwt.JwtAuthenticationFilter;
import com.anonymouschat.anonymouschatserver.common.jwt.JwtTokenProvider;
import com.anonymouschat.anonymouschatserver.domain.user.repository.UserRepository;
import com.anonymouschat.anonymouschatserver.common.security.OAuth2LoginSuccessHandler;
import com.anonymouschat.anonymouschatserver.common.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;
	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
				// ✅ CSRF 비활성화 & CORS 기본 설정
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())

				// ✅ 세션 stateless 설정 (JWT 기반)
				.sessionManagement(session -> session
						                              .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				// ✅ 인증 실패 시 JSON 에러 반환
				.exceptionHandling(exception -> exception
						                                .authenticationEntryPoint((request, response, authException) ->
								                                                          ResponseUtil.writeUnauthorizedResponse(response, "인증이 필요합니다."))
				)

				// ✅ 경로별 접근 제어 설정
				.authorizeHttpRequests(auth -> auth
						                               // 회원가입 완료 전 접근 가능한 경로
						                               .requestMatchers(
								                               "/api/v1/auth/**",           // 로그인, 회원가입
								                               "/oauth2/**"                 // OAuth2 callback
						                               ).permitAll()

						                               // 문서화 및 헬스체크
						                               .requestMatchers(
								                               "/swagger-ui.html",
								                               "/swagger-ui/**",
								                               "/v3/api-docs/**",
								                               "/actuator/health"
						                               ).permitAll()

						                               // 나머지 모든 경로는 인증 필요
						                               .anyRequest().authenticated()
				)

				// ✅ OAuth2 성공 후 JWT 반환 핸들러 연결
				.oauth2Login(oauth2 -> oauth2
						                       .successHandler(oAuth2LoginSuccessHandler)
				)

				// ✅ JWT 인증 필터 등록 (Spring Security 필터 체인 앞에 위치)
				.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(jwtTokenProvider, userRepository);
	}
}

