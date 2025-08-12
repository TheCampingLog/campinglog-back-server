package com.campinglog.campinglogbackserver.member.exception;

public class DuplicateNicknameError extends RuntimeException {
    public DuplicateNicknameError(String message) { super(message); }
}
