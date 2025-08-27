package com.anonymouschat.anonymouschatserver.infra.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth")
@Getter
@Setter
public class OAuthTokenProperties {

	private TempToken tempToken = new TempToken();
	private RefreshToken refreshToken = new RefreshToken();

	@Getter
	@Setter
	public static class TempToken {
		private long ttl = 300; // 5분
		private String prefix = "oauth:temp:";
	}

	@Getter
	@Setter
	public static class RefreshToken {
		private long ttl = 604800; // 7일
		private String prefix = "oauth:refresh:";
	}
}