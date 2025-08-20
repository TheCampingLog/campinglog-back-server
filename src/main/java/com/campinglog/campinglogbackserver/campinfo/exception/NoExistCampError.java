package com.campinglog.campinglogbackserver.campinfo.exception;

public class NoExistCampError extends RuntimeException {

  public NoExistCampError(String message) {
    super(message);
  }
}
