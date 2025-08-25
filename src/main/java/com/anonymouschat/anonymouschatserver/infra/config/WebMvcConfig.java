package com.anonymouschat.anonymouschatserver.infra.config;

import com.anonymouschat.anonymouschatserver.presentation.interceptor.LastActiveInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

	private final LastActiveInterceptor lastActiveInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(lastActiveInterceptor)
				.addPathPatterns("/api/**");
	}
}
