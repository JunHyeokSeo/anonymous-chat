package com.anonymouschat.anonymouschatserver.common.jwt;

import com.anonymouschat.anonymouschatserver.common.security.OAuthPrincipal;
import com.anonymouschat.anonymouschatserver.common.security.PrincipalType;
import com.anonymouschat.anonymouschatserver.common.security.TempOAuthPrincipal;
import com.anonymouschat.anonymouschatserver.common.security.UserPrincipal;
import com.anonymouschat.anonymouschatserver.domain.user.type.OAuthProvider;
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

	// 🔹 OAuth 회원가입 전 임시 토큰
	public String createTemporaryToken(OAuthProvider provider, String providerId) {
		return createToken(builder -> builder
				                              .claim("provider", provider.name())
				                              .claim("providerId", providerId)
				                              .claim("purpose", PrincipalType.REGISTRATION.name()),
				accessTokenValidityInSeconds
		);
	}

	// 🔹 사용자 토큰 (로그인 이후)
	public String createUserToken(Long userId) {
		return createToken(builder -> builder
				                              .claim("userId", userId)
				                              .claim("role", "USER")
				                              .claim("purpose", PrincipalType.ACCESS.name()),
				accessTokenValidityInSeconds
		);
	}

	// 🔹 Refresh 토큰 (목적 없음, 별도 사용)
	public String createRefreshToken(Long userId) {
		return createToken(builder -> builder
				                              .claim("userId", userId),
				refreshTokenValidityInSeconds
		);
	}

	// 🔹 공통 토큰 생성
	private String createToken(Consumer<JwtBuilder> claimsConfigurer, long validitySeconds) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + validitySeconds * 1000);

		JwtBuilder builder = Jwts.builder()
				                     .setIssuedAt(now)
				                     .setExpiration(expiry);

		claimsConfigurer.accept(builder);

		return builder.signWith(secretKey, SignatureAlgorithm.HS256).compact();
	}

	// 🔹 JWT 유효성 검사
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

	// 🔹 JWT에서 Principal 추출 및 purpose 체크
	public OAuthPrincipal getPrincipalFromToken(String token) {
		Claims claims = parse(token);

		String purpose = claims.get("purpose", String.class);
		if (purpose == null) {
			throw new JwtException("JWT에 purpose가 존재하지 않습니다.");
		}

		PrincipalType type;
		try {
			type = PrincipalType.valueOf(purpose);
		} catch (IllegalArgumentException e) {
			throw new JwtException("유효하지 않은 토큰 목적입니다: " + purpose);
		}

		return switch (type) {
			case ACCESS -> {
				Long userId = claims.get("userId", Number.class).longValue();
				yield new UserPrincipal(userId);
			}
			case REGISTRATION -> {
				OAuthProvider provider = OAuthProvider.valueOf(claims.get("provider", String.class));
				String providerId = claims.get("providerId", String.class);
				yield new TempOAuthPrincipal(provider, providerId);
			}
		};
	}

	// 🔹 내부 파싱 유틸
	private Claims parse(String token) {
		return Jwts.parserBuilder()
				       .setSigningKey(secretKey)
				       .build()
				       .parseClaimsJws(token)
				       .getBody();
	}
}