package com.campinglog.campinglogbackserver.member.exception;

public class PasswordMismatchError extends RuntimeException {
    public PasswordMismatchError(String message) {
        super(message);
    }
}
