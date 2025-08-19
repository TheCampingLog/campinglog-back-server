package com.campinglog.campinglogbackserver.board.exception;

public class NotYourBoardError extends RuntimeException {

    public NotYourBoardError(String message) {
        super(message);
    }
}
