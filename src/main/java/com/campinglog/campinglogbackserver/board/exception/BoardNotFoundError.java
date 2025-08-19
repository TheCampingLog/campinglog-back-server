package com.campinglog.campinglogbackserver.board.exception;

public class BoardNotFoundError extends RuntimeException {

    public BoardNotFoundError(String message) {
        super(message);
    }
}
