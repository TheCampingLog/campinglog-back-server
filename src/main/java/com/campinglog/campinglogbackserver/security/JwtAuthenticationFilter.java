package com.campinglog.campinglogbackserver.security;

import com.campinglog.campinglogbackserver.member.dto.request.RequestLogin;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Log4j2
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtProperties jwtProperties;
  private final RefreshProperties refreshProperties;
  private final RefreshTokenProvider refreshTokenProvider;
  private final RefreshTokenService refreshTokenService;

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) throws AuthenticationException {

    log.info("attemptAuthentication = 로그인 시도");
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      RequestLogin requestLogin = objectMapper.readValue(request.getInputStream(),
          RequestLogin.class);
      UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
          requestLogin.getEmail(), requestLogin.getPassword());
      Authentication authentication = authenticationManager.authenticate(authRequest);

      return authentication;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain, Authentication authResult) throws IOException, ServletException {
    Member member = ((CustomUserDetails) authResult.getPrincipal()).getUser();

    String jwtToken = jwtTokenProvider.generateToken(member);
    String refreshToken = refreshTokenProvider.generateToken();

    refreshTokenService.saveRefreshToken(refreshToken, member);
    addRefreshTokenCookie(response, refreshToken);

    response.addHeader(jwtProperties.getHeaderString(), jwtProperties.getTokenPrefix() + jwtToken);

    response.getWriter().println(Map.of("message", "login_success"));
  }

  @Override
  protected void unsuccessfulAuthentication(HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException failed) throws IOException, ServletException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.getWriter().println(Map.of("message", "login_fail"));
  }

  private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
    response.setHeader("Set-Cookie",
        String.format("%s=%s; Max-Age=%d; Path=%s; HttpOnly; SameSite=%s",
            refreshProperties.getCookie(),
            refreshToken, refreshProperties.getExpiration(), "/", "Strict"));
  }
}
