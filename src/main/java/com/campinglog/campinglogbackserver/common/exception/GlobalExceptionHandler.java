package com.campinglog.campinglogbackserver.common.exception;

import static com.campinglog.campinglogbackserver.common.exception.ExceptionHandlerUtil.buildResponse;

import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<CustomErrorResponse> handleValidationException(
      MethodArgumentNotValidException e,
      HttpServletRequest request) {
    String message = e.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));
    return buildResponse(request, HttpStatus.BAD_REQUEST, "유효성 검증 실패: " + message);
  }

}
