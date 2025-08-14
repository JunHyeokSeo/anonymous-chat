package com.anonymouschat.anonymouschatserver.infra.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
		info = @Info(title = "Anonymous Chat API", version = "v1", description = "1:1 익명 채팅 서비스 API 문서")
)
@SecurityScheme(
		name = "access-token",
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT"
)
@Configuration
public class SwaggerConfig {
}
