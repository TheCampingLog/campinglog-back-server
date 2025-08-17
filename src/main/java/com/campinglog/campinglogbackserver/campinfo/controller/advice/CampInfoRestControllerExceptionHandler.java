package com.campinglog.campinglogbackserver.campinfo.controller.advice;

import static com.campinglog.campinglogbackserver.common.exception.ExceptionHandlerUtil.buildResponse;

import com.campinglog.campinglogbackserver.campinfo.exception.ApiParsingError;
import com.campinglog.campinglogbackserver.campinfo.exception.CallCampApiError;
import com.campinglog.campinglogbackserver.campinfo.exception.InvalidLimitError;
import com.campinglog.campinglogbackserver.campinfo.exception.NoExistReviewOfBoardError;
import com.campinglog.campinglogbackserver.campinfo.exception.NullReviewError;
import com.campinglog.campinglogbackserver.common.exception.CustomErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Log4j2
@RestControllerAdvice(basePackages = "com/campinglog/campinglogbackserver/campinfo")
public class CampInfoRestControllerExceptionHandler {

  @ExceptionHandler(NullReviewError.class)
  public ResponseEntity<CustomErrorResponse> handleNullReview(Exception e, HttpServletRequest request) {
    log.warn("NullReviewError: {}", e.getMessage(), e);
    return buildResponse(request, HttpStatus.BAD_REQUEST, "요청 처리 중 문제가 발생했습니다. error: " + e.getMessage());
  }

  @ExceptionHandler(NoExistReviewOfBoardError.class)
  public ResponseEntity<CustomErrorResponse> handleNullReviewOfBoard(Exception e, HttpServletRequest request) {
    log.warn("NullReviewError: {}", e.getMessage(), e);
    return buildResponse(request, HttpStatus.BAD_REQUEST, "요청 처리 중 문제가 발생했습니다. error: " + e.getMessage());
  }

  @ExceptionHandler(ApiParsingError.class)
  public ResponseEntity<CustomErrorResponse> handleApiParcing(Exception e, HttpServletRequest request) {
    log.warn("ApiParsingError: {}", e.getMessage(), e);
    return buildResponse(request, HttpStatus.BAD_REQUEST, "요청 처리 중 문제가 발생했습니다. error: " + e.getMessage());
  }

  @ExceptionHandler(CallCampApiError.class)
  public  ResponseEntity<CustomErrorResponse> handleCallCamp(Exception e, HttpServletRequest request) {
    log.warn("CallCampApiError: {}", e.getMessage(), e);
    return buildResponse(request, HttpStatus.BAD_REQUEST, "요청 처리 중 문제가 발생했습니다. error: " + e.getMessage());
  }

  @ExceptionHandler(InvalidLimitError.class)
  public  ResponseEntity<CustomErrorResponse> handleInvalidLimit(Exception e, HttpServletRequest request) {
    log.warn("InvalidLimitError: {}", e.getMessage(), e);
    return buildResponse(request, HttpStatus.BAD_REQUEST, "요청 처리 중 문제가 발생했습니다. error: " + e.getMessage());
  }
}
