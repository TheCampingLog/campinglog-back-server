package com.campinglog.campinglogbackserver.campinfo.controller.advice;

import static com.campinglog.campinglogbackserver.common.exception.ExceptionHandlerUtil.buildResponse;

import com.campinglog.campinglogbackserver.campinfo.exception.NullReviewError;
import com.campinglog.campinglogbackserver.common.exception.CustomErrorResponse;
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

  @ExceptionHandler(NullReviewError.class)
  public ResponseEntity<CustomErrorResponse> handleNull(Exception e, HttpServletRequest request) {
    log.warn("NullReviewError: {}", e.getMessage(), e);
    return buildResponse(request, HttpStatus.BAD_REQUEST, "요청 처리 중 문제가 발생했습니다. error: " + e.getMessage());
  }
}
