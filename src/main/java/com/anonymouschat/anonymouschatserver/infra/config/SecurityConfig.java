package com.anonymouschat.anonymouschatserver.infra.config;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.infra.security.OAuth2AuthenticationSuccessHandler;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtAuthenticationFactory;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtAuthenticationFilter;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtTokenResolver;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtValidator;
import com.anonymouschat.anonymouschatserver.infra.web.ApiResponse;
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
	private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
	private final JwtTokenResolver tokenResolver;
	private final JwtValidator jwtValidator;
	private final JwtAuthenticationFactory authFactory;

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
								                                                          ApiResponse.writeErrorResponse(response, ErrorCode.UNAUTHORIZED,"인증이 필요합니다."))
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
				.addFilterBefore(jwtAuthenticationFilter(tokenResolver, jwtValidator, authFactory), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter(
			JwtTokenResolver tokenResolver,
			JwtValidator jwtValidator,
			JwtAuthenticationFactory authFactory
	) {
		return new JwtAuthenticationFilter(tokenResolver, jwtValidator, authFactory);
	}
}
