package com.anonymouschat.anonymouschatserver.common.jwt;

import com.anonymouschat.anonymouschatserver.common.enums.UserRole;
import com.anonymouschat.anonymouschatserver.common.security.CustomPrincipal;
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
		} catch (SecurityException | MalformedJwtException e) {
			throw new JwtException("잘못된 JWT 서명입니다.", e);
		} catch (ExpiredJwtException e) {
			throw new JwtException("만료된 JWT 입니다.", e);
		} catch (UnsupportedJwtException e) {
			throw new JwtException("지원하지 않는 JWT 입니다.", e);
		} catch (IllegalArgumentException e) {
			throw new JwtException("JWT claims 문자열이 비어있습니다.", e);
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
		throw new JwtException("유효한 클레임이 JWT에 존재하지 않습니다.");
	}

	private Claims parse(String token) {
		return Jwts.parserBuilder()
				       .setSigningKey(secretKey)
				       .build()
				       .parseClaimsJws(token)
				       .getBody();
	}
}
