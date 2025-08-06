package com.anonymouschat.anonymouschatserver.common.config;

import com.anonymouschat.anonymouschatserver.common.jwt.JwtAuthenticationFilter;
import com.anonymouschat.anonymouschatserver.common.jwt.JwtTokenProvider;
import com.anonymouschat.anonymouschatserver.common.security.OAuth2AuthenticationSuccessHandler;
import com.anonymouschat.anonymouschatserver.common.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
	private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
				//CORS + CSRF 비활성화
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())

				//세션 정책: STATELESS
				.sessionManagement(session -> session
						                              .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				//인증 실패 시 JSON 응답
				.exceptionHandling(exception -> exception
						                                .authenticationEntryPoint((request, response, authException) ->
								                                                          ResponseUtil.writeUnauthorizedResponse(response, "인증이 필요합니다."))
				)

				//접근 허용 경로 설정
				.authorizeHttpRequests(auth -> auth
						                               .requestMatchers(
								                               "/api/v1/auth/**",           // 로그인, 회원가입
								                               "/oauth2/**",                // OAuth2 로그인
								                               "/swagger-ui.html",
								                               "/swagger-ui/**",
								                               "/v3/api-docs/**",
								                               "/actuator/health"
						                               ).permitAll()
						                               .anyRequest().authenticated()
				)

				//OAuth2 로그인 성공 시 토큰 발급 핸들러 연결
				.oauth2Login(oauth2 -> oauth2
						                       .successHandler(oAuth2AuthenticationSuccessHandler)
				)

				//불필요한 form 로그인 제거
				.formLogin(AbstractHttpConfigurer::disable)

				//JWT 인증 필터 추가
				.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(jwtTokenProvider);
	}
}
