package com.emr.security;

import com.emr.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
  private final Key signingKey;
  private final long ttlSeconds;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.ttlSeconds:28800}") long ttlSeconds
  ) {
    this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    this.ttlSeconds = ttlSeconds;
  }

  public String generateToken(String subject, Role role) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(ttlSeconds);
    return Jwts.builder()
        .setSubject(subject)
        .addClaims(Map.of("role", role.name()))
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(exp))
        .signWith(signingKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public String extractSubject(String token) {
    try {
      return parse(token).getSubject();
    } catch (Exception ignored) {
      return null;
    }
  }

  public boolean isTokenValid(String token, String expectedEmail) {
    try {
      Claims claims = parse(token);
      String sub = claims.getSubject();
      Date exp = claims.getExpiration();
      return sub != null
          && sub.equalsIgnoreCase(expectedEmail)
          && exp != null
          && exp.after(new Date());
    } catch (Exception ignored) {
      return false;
    }
  }

  private Claims parse(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(signingKey)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }
}
