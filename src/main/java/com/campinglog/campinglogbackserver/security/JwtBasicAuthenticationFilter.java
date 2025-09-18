package com.campinglog.campinglogbackserver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class JwtBasicAuthenticationFilter extends BasicAuthenticationFilter {

  private final JwtProperties jwtProperties;

  public JwtBasicAuthenticationFilter(AuthenticationManager authenticationManager
      , JwtProperties jwtProperties) {
    super(authenticationManager);
    this.jwtProperties = jwtProperties;
  }

  @Override
  protected boolean shouldNotFilterAsyncDispatch() {
    return false;
  }

  @Override
  protected boolean shouldNotFilterErrorDispatch() {
    return false;
  }

  @Override
  public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    String jwtHeader = request.getHeader(jwtProperties.getHeaderString());
    if (jwtHeader == null || !jwtHeader.startsWith(jwtProperties.getTokenPrefix())) {
      chain.doFilter(request, response);
      return;
    }

    String token = jwtHeader.replace(jwtProperties.getTokenPrefix(), "");

    try {
      // JWT secret key 설정 (실제로는 jwtProperties에서 가져와야 함)
      byte[] signingKey = jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);

      Claims claims = Jwts.parser()
          .verifyWith(Keys.hmacShaKeyFor(signingKey))
          .build()
          .parseClaimsJws(token)
          .getBody();

      String email = claims.get("email", String.class);
      String role = claims.get("role", String.class); // role 클레임 추출

      if (email != null && !email.isEmpty()) {
        // 권한 설정
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (role != null && !role.isEmpty()) {
          // role이 "ADMIN", "USER" 등의 형태라면 "ROLE_" prefix 추가
          authorities.add(new SimpleGrantedAuthority(role));
        }

        // Authentication 객체 생성 및 SecurityContext에 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            email, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

    } catch (ExpiredJwtException eje) {
      // 토큰 만료시 401 응답 및 필터 체인 종료
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\": \"Token expired\"}");
      return;
    } catch (Exception e) {
      logger.error("JWT token validation failed: " + e.getMessage());
      // 토큰 오류시 401 처리 (필요 시)
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\": \"Invalid token\"}");
      return;
    }

    chain.doFilter(request, response);
  }
}
