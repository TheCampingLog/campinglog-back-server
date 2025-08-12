package com.campinglog.campinglogbackserver.member.exception;

import static com.campinglog.campinglogbackserver.common.exception.ExceptionHandlerUtil.buildResponse;

import com.campinglog.campinglogbackserver.common.exception.CustomErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.campinglog.campinglogbackserver.member")
public class MemberExceptionHandler {

  @ExceptionHandler(MemberCreationError.class)
  public ResponseEntity<CustomErrorResponse> handleDataAccess(MemberCreationError e,
      HttpServletRequest request) {
    return buildResponse(request, HttpStatus.INTERNAL_SERVER_ERROR,
        e.getMessage());
  }

  @ExceptionHandler(DuplicateEmailError.class)
  public ResponseEntity<CustomErrorResponse> handleDupEmail(DuplicateEmailError e,
                                                            HttpServletRequest request) {
    return buildResponse(request, HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler(DuplicateNicknameError.class)
  public ResponseEntity<CustomErrorResponse> handleDupNickname(DuplicateNicknameError e,
                                                               HttpServletRequest request) {
    return buildResponse(request, HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler(PasswordMismatchError.class)
  public ResponseEntity<CustomErrorResponse> handlePwMismatch(PasswordMismatchError e,
                                                              HttpServletRequest request) {
    return buildResponse(request, HttpStatus.UNAUTHORIZED, e.getMessage());
  }

  @ExceptionHandler(MemberNotFoundError.class)
  public ResponseEntity<CustomErrorResponse> handleMemberNotFound(MemberNotFoundError e,
                                                                  HttpServletRequest request) {
    return buildResponse(request, HttpStatus.NOT_FOUND, e.getMessage());
  }
}
