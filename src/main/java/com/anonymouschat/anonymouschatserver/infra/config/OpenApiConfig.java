package com.anonymouschat.anonymouschatserver.infra.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
		info = @Info(
				title = "Anonymous Chat API",
				version = "v1",
				description = "1:1 익명 채팅 애플리케이션 API 명세서",
				contact = @Contact(name = "서준혁", email = "sjhgd107@naver.com"),
				license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")
		),
		security = { @SecurityRequirement(name = "BearerAuth") }
)
@SecurityScheme(
		name = "BearerAuth",
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT"
)
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		Schema<?> commonResponseSchema = new Schema<>().$ref("#/components/schemas/CommonResponse");

		return new OpenAPI()
				       .components(new Components()
						                   .addResponses("Success", new ApiResponse()
								                                            .description("요청 성공")
								                                            .content(new Content().addMediaType("application/json",
										                                            new MediaType().schema(commonResponseSchema))))
						                   .addResponses("Created", new ApiResponse()
								                                            .description("리소스 생성 성공")
								                                            .content(new Content().addMediaType("application/json",
										                                            new MediaType().schema(commonResponseSchema))))
						                   .addResponses("BadRequest", new ApiResponse()
								                                               .description("잘못된 요청")
								                                               .content(new Content().addMediaType("application/json",
										                                               new MediaType().schema(commonResponseSchema))))
						                   .addResponses("Unauthorized", new ApiResponse()
								                                                 .description("인증 실패")
								                                                 .content(new Content().addMediaType("application/json",
										                                                 new MediaType().schema(commonResponseSchema))))
				       );
	}
}
