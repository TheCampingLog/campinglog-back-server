package com.campinglog.campinglogbackserver.security;

import com.campinglog.campinglogbackserver.member.dto.request.RequestLogin;
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
    log.info("로그인 성공");
    CustomUserDetails details = (CustomUserDetails) authResult.getPrincipal();
    String jwtToken = jwtTokenProvider.generateToken(
        ((CustomUserDetails) authResult.getPrincipal()).getUser());

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
}
