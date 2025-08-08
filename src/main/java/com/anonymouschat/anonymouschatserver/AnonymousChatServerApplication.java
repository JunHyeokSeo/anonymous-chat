package com.anonymouschat.anonymouschatserver;

import com.anonymouschat.anonymouschatserver.infra.security.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class AnonymousChatServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnonymousChatServerApplication.class, args);
	}

}
