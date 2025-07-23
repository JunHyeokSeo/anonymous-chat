package com.anonymouschat.anonymouschatserver.common.config;

import com.anonymouschat.anonymouschatserver.common.resolver.OAuthPrincipalArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final OAuthPrincipalArgumentResolver oAuthPrincipalArgumentResolver;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(oAuthPrincipalArgumentResolver);
	}
}
