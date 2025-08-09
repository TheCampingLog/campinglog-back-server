package com.campinglog.campinglogbackserver.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ExceptionHandlerUtil {

  public static ResponseEntity<CustomErrorResponse> buildResponse(HttpServletRequest request,
      HttpStatus status,
      String message) {
    CustomErrorResponse response = CustomErrorResponse.builder()
        .path(request.getRequestURI())
        .message(message)
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(status).body(response);
  }

}
