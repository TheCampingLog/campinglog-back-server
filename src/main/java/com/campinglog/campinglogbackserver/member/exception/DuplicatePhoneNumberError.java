package com.campinglog.campinglogbackserver.member.exception;

public class DuplicatePhoneNumberError extends RuntimeException {
    public DuplicatePhoneNumberError(String message) {super(message);}
}
