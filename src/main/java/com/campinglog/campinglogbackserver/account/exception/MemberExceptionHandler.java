package com.campinglog.campinglogbackserver.account.exception;

import static com.campinglog.campinglogbackserver.common.exception.ExceptionHandlerUtil.buildResponse;

import com.campinglog.campinglogbackserver.common.exception.CustomErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.campinglog.campinglogbackserver.account")
public class MemberExceptionHandler {

  @ExceptionHandler(MemberCreationError.class)
  public ResponseEntity<CustomErrorResponse> handleDataAccess(MemberCreationError e,
      HttpServletRequest request) {
    return buildResponse(request, HttpStatus.INTERNAL_SERVER_ERROR,
        e.getMessage());
  }

}
