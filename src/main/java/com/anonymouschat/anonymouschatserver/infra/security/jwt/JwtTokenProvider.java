package com.anonymouschat.anonymouschatserver.infra.security.jwt;

import com.anonymouschat.anonymouschatserver.common.code.ErrorCode;
import com.anonymouschat.anonymouschatserver.common.exception.auth.InvalidTokenException;
import com.anonymouschat.anonymouschatserver.domain.type.UserRole;
import com.anonymouschat.anonymouschatserver.infra.security.CustomPrincipal;
import com.anonymouschat.anonymouschatserver.domain.type.OAuthProvider;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
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

	private Key secretKey;

	@PostConstruct
	public void init() {
		this.secretKey = Keys.hmacShaKeyFor(secretKeyPlain.getBytes());
	}

	//회원가입 전 토큰
	public String createAccessTokenForOAuthLogin(OAuthProvider provider, String providerId) {
		return createToken(builder -> builder
				                              .claim("role", UserRole.ROLE_GUEST.getAuthority())
				                              .claim("provider", provider.name())
				                              .claim("providerId", providerId),
				accessTokenValidityInSeconds
		);
	}

	//회원가입 완료 후 토큰 (userId 기반)
	public String createAccessToken(Long userId, String role) {
		return createToken(builder -> builder
				                              .claim("role", role)
				                              .claim("userId", userId),
				accessTokenValidityInSeconds
		);
	}

	public String createRefreshToken(Long userId, String role) {
		return createToken(builder -> builder
				                              .claim("role", role)
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

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
			return true;
		} catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
			throw new InvalidTokenException(ErrorCode.INVALID_TOKEN, e);
		} catch (ExpiredJwtException e) {
			throw new InvalidTokenException(ErrorCode.EXPIRED_TOKEN, e);
		}
	}

	public CustomPrincipal getPrincipalFromToken(String token) {
		Claims c = parse(token);
		Long userId = c.get("userId", Long.class);
		String providerName = c.get("provider", String.class);
		String providerId = c.get("providerId", String.class);
		String role = c.get("role", String.class);

		if (userId != null) {
			return CustomPrincipal.builder()
					       .userId(userId)
					       .role(role)
					       .build();
		}

		// OAuth 임시 사용자의 경우만 provider 정보 검사
		if (providerName != null && providerId != null) {
			return CustomPrincipal.builder()
					       .provider(OAuthProvider.valueOf(providerName))
					       .providerId(providerId)
					       .role(role)
					       .build();
		}
		throw new InvalidTokenException(ErrorCode.INVALID_TOKEN);
	}

	private Claims parse(String token) {
		return Jwts.parserBuilder()
				       .setSigningKey(secretKey)
				       .build()
				       .parseClaimsJws(token)
				       .getBody();
	}
}