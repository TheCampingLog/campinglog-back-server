package com.campinglog.campinglogbackserver.member.exception;

public class DuplicateEmailError extends RuntimeException {

    public DuplicateEmailError(String message) { super(message); }
}
