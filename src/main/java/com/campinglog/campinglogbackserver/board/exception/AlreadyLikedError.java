package com.campinglog.campinglogbackserver.board.exception;

public class AlreadyLikedError extends RuntimeException {

    public AlreadyLikedError(String message) {
        super(message);
    }
}
