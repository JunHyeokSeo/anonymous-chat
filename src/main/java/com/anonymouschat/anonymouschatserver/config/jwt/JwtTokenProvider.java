package com.anonymouschat.anonymouschatserver.config.jwt;

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

	public String createAccessToken(Long userId) {
		return createToken(userId, accessTokenValidityInSeconds);
	}

	public String createRefreshToken(Long userId) {
		return createToken(userId, refreshTokenValidityInSeconds);
	}

	private String createToken(Long userId, long validitySeconds) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + validitySeconds * 1000);

		return Jwts.builder()
				       .setSubject(String.valueOf(userId))
				       .setIssuedAt(now)
				       .setExpiration(expiry)
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

	public Long getUserIdFromToken(String token) {
		Claims claims = Jwts.parserBuilder()
				                .setSigningKey(secretKey)
				                .build()
				                .parseClaimsJws(token)
				                .getBody();

		return Long.parseLong(claims.getSubject());
	}
}

