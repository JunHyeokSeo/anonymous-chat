package com.anonymouschat.anonymouschatserver.infra.config;

import com.anonymouschat.anonymouschatserver.infra.security.CustomAccessDeniedHandler;
import com.anonymouschat.anonymouschatserver.infra.security.CustomAuthenticationEntryPoint;
import com.anonymouschat.anonymouschatserver.infra.security.OAuth2AuthenticationSuccessHandler;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtAuthenticationFactory;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtAuthenticationFilter;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtTokenResolver;
import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity()
@RequiredArgsConstructor
public class SecurityConfig {

	private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
	private final JwtTokenResolver tokenResolver;
	private final JwtValidator jwtValidator;
	private final JwtAuthenticationFactory authFactory;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				// CORS/CSRF
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())

				// 세션: STATELESS
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				// 401/403 JSON 통일
				.exceptionHandling(ex -> ex
						                         .authenticationEntryPoint(customAuthenticationEntryPoint())
						                         .accessDeniedHandler(customAccessDeniedHandler())
				)

				// 인가 규칙
				.authorizeHttpRequests(auth -> auth
						                               .requestMatchers(
								                               "/api/v1/auth/**",
								                               "/oauth2/**",
								                               "/swagger-ui.html",
								                               "/swagger-ui/**",
								                               "/v3/api-docs/**",
								                               "/actuator/health"
						                               ).permitAll()
						                               .anyRequest().authenticated()
				)

				// OAuth2 로그인
				.oauth2Login(oauth2 -> oauth2
						                       .successHandler(oAuth2AuthenticationSuccessHandler)
				)

				// 폼 로그인 제거
				.formLogin(AbstractHttpConfigurer::disable)

				// JWT 필터 등록 ExceptionTranslationFilter
				.addFilterAfter(jwtAuthenticationFilter(tokenResolver, jwtValidator, authFactory),
						ExceptionTranslationFilter.class);

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

	@Bean
	public CustomAuthenticationEntryPoint customAuthenticationEntryPoint() {
		return new CustomAuthenticationEntryPoint();
	}

	@Bean
	public CustomAccessDeniedHandler customAccessDeniedHandler() {
		return new CustomAccessDeniedHandler();
	}
}