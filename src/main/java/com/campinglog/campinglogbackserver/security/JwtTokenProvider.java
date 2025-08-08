package com.campinglog.campinglogbackserver.security;

import com.campinglog.campinglogbackserver.account.entity.Member;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

  private final JwtProperties jwtProperties;

  public String generateToken(Member user) {
    Date now = new Date();
    return makeToken(new Date(now.getTime() + jwtProperties.getExpiration()), user);
  }

  private String makeToken(Date expiry, Member user) {
    Date now = new Date();

    return Jwts.builder()
        .issuer(jwtProperties.getIssuer())
        .issuedAt(now)
        .expiration(expiry)
        .claim("email", user.getEmail())
        .claim("role", "ROLE_" + user.getRole())
        .signWith(Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8)))
        .compact();
  }

  private boolean validateJwtToken(String token) {
    try {
      Jwts.parser().decryptWith(
          Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8)));
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

}
