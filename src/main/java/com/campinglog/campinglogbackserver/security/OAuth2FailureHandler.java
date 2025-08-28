package com.campinglog.campinglogbackserver.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {

    if (exception == null || exception.getMessage() == null) {
      log.error("OAuth2 로그인 실패: 알 수 없는 오류 발생");
      response.sendRedirect("/login?error=unknown");
      return;
    }

    String errorMessage = exception.getMessage();

    // You can customize error handling based on exception message or type
    if (errorMessage.contains("invalid_token")) {
      log.error("OAuth2 로그인 실패: 토큰이 유효하지 않음");
      response.sendRedirect("/login?error=invalid_token");
    } else if (errorMessage.contains("access_denied")) {
      log.error("OAuth2 로그인 실패: 접근 거부");
      response.sendRedirect("/login?error=access_denied");
    } else {
      log.error("OAuth2 로그인 실패: {}", errorMessage);
      response.sendRedirect("/login?error=true");
    }
  }

}
