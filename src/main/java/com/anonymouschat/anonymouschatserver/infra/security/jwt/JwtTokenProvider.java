package com.anonymouschat.anonymouschatserver.infra.security.jwt;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import com.anonymouschat.anonymouschatserver.domain.type.Role;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Consumer;

@Component
public class JwtTokenProvider {

	@Value("${jwt.secret}")
	private String secretKeyPlain;

	@Value("${jwt.access-token-expiration}")
	private long accessTokenValidityInSeconds;

	@Value("${jwt.refresh-token-expiration}")
	private long refreshTokenValidityInSeconds;

	@Getter
	private Key secretKey;

	@PostConstruct
	public void init() {
		this.secretKey = Keys.hmacShaKeyFor(secretKeyPlain.getBytes());
	}

	public String createAccessTokenForOAuthLogin(OAuthProvider provider, String providerId) {
		return createToken(builder -> builder
				                              .claim("role", Role.GUEST.name())
				                              .claim("provider", provider.name())
				                              .claim("providerId", providerId),
				accessTokenValidityInSeconds
		);
	}

	public String createAccessToken(Long userId, Role role) {
		return createToken(builder -> builder
				                              .claim("role", role.name())
				                              .claim("userId", userId),
				accessTokenValidityInSeconds
		);
	}

	public String createRefreshToken(Long userId, Role role) {
		return createToken(builder -> builder
				                              .claim("role", role.name())
				                              .claim("userId", userId),
				refreshTokenValidityInSeconds
		);
	}

	private String createToken(Consumer<JwtBuilder> claimsConfigurer, long validitySeconds) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + validitySeconds * 1000);

		JwtBuilder builder = Jwts.builder()
				                     .setIssuer("anonymous-chat-server")
				                     .setIssuedAt(now)
				                     .setExpiration(expiry);

		claimsConfigurer.accept(builder);

		return builder.signWith(secretKey, SignatureAlgorithm.HS256).compact();
	}

	public CustomPrincipal getPrincipalFromToken(String token) {
		Claims claims = parse(token);

		Long userId = claims.get("userId", Long.class);
		String providerName = claims.get("provider", String.class);
		String providerId = claims.get("providerId", String.class);
		String roleName = claims.get("role", String.class);
		Role role = Role.valueOf(roleName);

		if (userId != null) {
			return CustomPrincipal.builder()
					       .userId(userId)
					       .role(role)
					       .build();
		}

		if (providerName != null && providerId != null) {
			return CustomPrincipal.builder()
					       .provider(OAuthProvider.valueOf(providerName))
					       .providerId(providerId)
					       .role(role)
					       .build();
		}

		throw new InvalidTokenException(ErrorCode.INVALID_TOKEN);
	}

	public long getExpirationMillis(String token) {
		Claims claims = parse(token);
		long diff = claims.getExpiration().getTime() - System.currentTimeMillis();
		return Math.max(diff, 0);
	}

	public Claims parse(String token) {
		return Jwts.parserBuilder()
				       .setSigningKey(secretKey)
				       .build()
				       .parseClaimsJws(token)
				       .getBody();
	}
}