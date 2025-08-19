package com.campinglog.campinglogbackserver.board.exception;

public class CommentNotFoundError extends RuntimeException {

    public CommentNotFoundError(String message) {
        super(message);
    }
}
