package com.campinglog.campinglogbackserver.member.exception;

public class MemberNotFoundError extends RuntimeException {

    public MemberNotFoundError(String message) { super(message); }
}
