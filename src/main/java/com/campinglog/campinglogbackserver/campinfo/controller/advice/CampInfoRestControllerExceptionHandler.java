package com.campinglog.campinglogbackserver.campinfo.controller.advice;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice
public class CampInfoRestControllerExceptionHandler {
  private ResponseEntity<CustomErrorResponse> buildResponse(HttpServletRequest request, HttpStatus status,
      String message) {
    CustomErrorResponse response = CustomErrorResponse.builder()
        .path(request.getRequestURI())
        .message(message)
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(status).body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<CustomErrorResponse> handleNull(Exception e, HttpServletRequest request) {
    log.warn("IllegalArgument exception: {}", e.getMessage(), e);
    return buildResponse(request, HttpStatus.BAD_REQUEST, "요청 처리 중 문제가 발생했습니다. error: " + e.getMessage());
  }
}
