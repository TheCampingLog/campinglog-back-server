package com.campinglog.campinglogbackserver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenProvider {

  private final RefreshProperties refreshProperties;

  public String generateToken() {

    return Jwts.builder()
        .setSubject(UUID.randomUUID().toString())
        .setId(UUID.randomUUID().toString())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + refreshProperties.getExpiration()))
        .signWith(
            Keys.hmacShaKeyFor(refreshProperties.getSecretKey().getBytes(StandardCharsets.UTF_8)))
        .compact();

  }

  private boolean validateJwtToken(String token) {
    try {
      Jwts.parser().decryptWith(
          Keys.hmacShaKeyFor(refreshProperties.getSecretKey().getBytes(StandardCharsets.UTF_8)));
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }


  public Claims parseJwtToken(String token) {

    byte[] signingKey = refreshProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);

    return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(signingKey)).build().parseSignedClaims(token)
        .getPayload();
  }


}