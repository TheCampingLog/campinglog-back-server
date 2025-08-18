package com.campinglog.campinglogbackserver.member.exception;

public class InvalidPasswordError extends RuntimeException {
    public InvalidPasswordError(String message) {
        super(message);
    }
}
