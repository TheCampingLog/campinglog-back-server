package com.campinglog.campinglogbackserver.member.exception;

public class ProfileImageNotFoundError extends RuntimeException {
    public ProfileImageNotFoundError(String message) {
        super(message);
    }
}
