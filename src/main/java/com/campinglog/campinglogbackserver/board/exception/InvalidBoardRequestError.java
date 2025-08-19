package com.campinglog.campinglogbackserver.board.exception;

public class InvalidBoardRequestError extends RuntimeException {

    public InvalidBoardRequestError(String message) {
        super(message);
    }
}
