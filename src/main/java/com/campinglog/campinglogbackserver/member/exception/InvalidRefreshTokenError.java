package com.campinglog.campinglogbackserver.member.exception;

public class InvalidRefreshTokenError extends RuntimeException {

  public InvalidRefreshTokenError(String message) {
    super(message);
  }

}
