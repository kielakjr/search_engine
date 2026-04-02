package com.kielakjr.search_engine.auth;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

import com.kielakjr.search_engine.config.JwtProperties;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
  private final JwtProperties jwtProperties;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
  }

  public String generateToken(User user) {
    return Jwts.builder()
               .subject(user.getEmail())
               .claim("role", user.getRole().name())
               .issuedAt(new Date())
               .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
               .signWith(getSigningKey())
               .compact();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
      return !isTokenExpired(token);
    } catch (Exception e) {
      return false;
    }
  }

  public boolean isTokenValid(String token, User user) {
    String email = extractEmail(token);
    return email.equals(user.getEmail()) && !isTokenExpired(token);
  }

  public String extractEmail(String token) {
    return Jwts.parser()
               .verifyWith(getSigningKey())
               .build()
               .parseSignedClaims(token)
               .getPayload()
               .getSubject();
  }

  public boolean isTokenExpired(String token) {
    Date expiration = Jwts.parser()
                          .verifyWith(getSigningKey())
                          .build()
                          .parseSignedClaims(token)
                          .getPayload()
                          .getExpiration();
    return expiration.before(new Date());
  }
}
