package com.anonymouschat.anonymouschatserver.config;

import com.anonymouschat.anonymouschatserver.config.jwt.JwtAuthenticationFilter;
import com.anonymouschat.anonymouschatserver.config.jwt.JwtTokenProvider;
import com.anonymouschat.anonymouschatserver.global.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exception -> exception
						                                .authenticationEntryPoint((request, response, authException) ->
							                                ResponseUtil.writeUnauthorizedResponse(response, "인증이 필요합니다."))
				)
				.authorizeHttpRequests(auth -> auth
						                               .requestMatchers("/api/v1/auth/**").permitAll()
						                               .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
						                               .anyRequest().authenticated()
				)
				.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

		return http.build();

	}

	// (선택) AuthenticationManager bean 등록 - 필요 시
	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) {
		return http.getSharedObject(AuthenticationManager.class);
	}
}
