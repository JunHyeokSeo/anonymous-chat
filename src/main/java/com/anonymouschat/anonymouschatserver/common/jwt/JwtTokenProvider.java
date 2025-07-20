package com.anonymouschat.anonymouschatserver.common.jwt;

import com.anonymouschat.anonymouschatserver.domain.user.OAuthProvider;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

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

	public String createAccessToken(OAuthProvider provider, String providerId) {
		return createToken(provider, providerId, accessTokenValidityInSeconds);
	}

	public String createRefreshToken(OAuthProvider provider, String providerId) {
		return createToken(provider, providerId, refreshTokenValidityInSeconds);
	}

	private String createToken(OAuthProvider provider, String providerId, long validitySeconds) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + validitySeconds * 1000);

		return Jwts.builder()
				       .setIssuedAt(now)
				       .setExpiration(expiry)
				       .claim("provider", provider.name())
				       .claim("providerId", providerId)
				       .signWith(secretKey, SignatureAlgorithm.HS256)
				       .compact();
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

	public JwtUserInfo getUserInfoFromToken(String token) {
		Claims claims = Jwts.parserBuilder()
				                .setSigningKey(secretKey)
				                .build()
				                .parseClaimsJws(token)
				                .getBody();

		OAuthProvider provider = claims.get("provider", OAuthProvider.class);
		String providerId = claims.get("providerId", String.class);
		return new JwtUserInfo(provider, providerId);
	}
}

