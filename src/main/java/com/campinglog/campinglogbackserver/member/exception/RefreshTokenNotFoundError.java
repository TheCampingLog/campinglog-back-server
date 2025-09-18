package com.campinglog.campinglogbackserver.member.exception;

public class RefreshTokenNotFoundError extends RuntimeException {

  public RefreshTokenNotFoundError(String message) {
    super(message);
  }

}
