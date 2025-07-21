package com.anonymouschat.anonymouschatserver.config;

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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;
	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(jwtTokenProvider, userRepository);
	}

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
						                               .requestMatchers("/api/v1/auth/**", "/oauth2/**").permitAll()
						                               .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
						                               .anyRequest().authenticated()
				)
				.oauth2Login(oauth2 -> oauth2
						                       .successHandler(oAuth2LoginSuccessHandler)
				)
				.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();

	}

	// (선택) AuthenticationManager bean 등록 - 필요 시
	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) {
		return http.getSharedObject(AuthenticationManager.class);
	}
}
