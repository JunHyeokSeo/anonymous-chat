package com.anonymouschat.anonymouschatserver.common.jwt;

import com.anonymouschat.anonymouschatserver.common.security.OAuthPrincipal;
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
	public String createAccessToken(OAuthProvider provider, String providerId) {
		return createToken(builder -> builder
				                              .claim("provider", provider.name())
				                              .claim("providerId", providerId),
				accessTokenValidityInSeconds
		);
	}

	public String createRefreshToken(OAuthProvider provider, String providerId) {
		return createToken(builder -> builder
				                              .claim("provider", provider.name())
				                              .claim("providerId", providerId),
				refreshTokenValidityInSeconds
		);
	}

	//회원가입 완료 후 토큰 (userId 기반)
	public String createAccessToken(Long userId) {
		return createToken(builder -> builder
				                              .claim("userId", userId),
				accessTokenValidityInSeconds
		);
	}

	public String createRefreshToken(Long userId) {
		return createToken(builder -> builder
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
		} catch (SecurityException | MalformedJwtException e) {
			throw new JwtException("잘못된 JWT 서명입니다.", e);
		} catch (ExpiredJwtException e) {
			throw new JwtException("만료된 JWT입니다.", e);
		} catch (UnsupportedJwtException e) {
			throw new JwtException("지원하지 않는 JWT입니다.", e);
		} catch (IllegalArgumentException e) {
			throw new JwtException("JWT claims 문자열이 비어있습니다.", e);
		}
	}

	//Principal 추출: 회원가입 전
	public OAuthPrincipal getPrincipalFromToken(String token) {
		Claims claims = parse(token);

		String providerName = claims.get("provider", String.class);
		String providerId = claims.get("providerId", String.class);

		if (providerName == null || providerId == null) {
			throw new JwtException("provider 정보가 JWT에 존재하지 않습니다.");
		}

		OAuthProvider provider = OAuthProvider.valueOf(providerName);
		return new OAuthPrincipal(provider, providerId);
	}

	//userId 추출: 회원가입 완료 후
	public Long getUserIdFromToken(String token) {
		Claims claims = parse(token);
		Object raw = claims.get("userId");
		if (raw == null) {
			throw new JwtException("userId 정보가 JWT에 존재하지 않습니다.");
		}
		return Long.valueOf(raw.toString());
	}

	private Claims parse(String token) {
		return Jwts.parserBuilder()
				       .setSigningKey(secretKey)
				       .build()
				       .parseClaimsJws(token)
				       .getBody();
	}
}
