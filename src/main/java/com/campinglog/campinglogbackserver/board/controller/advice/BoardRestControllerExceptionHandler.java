package com.campinglog.campinglogbackserver.board.controller.advice;

import com.campinglog.campinglogbackserver.board.exception.AlreadyLikedError;
import com.campinglog.campinglogbackserver.board.exception.BoardNotFoundError;
import com.campinglog.campinglogbackserver.board.exception.CommentNotFoundError;
import com.campinglog.campinglogbackserver.board.exception.InvalidBoardRequestError;
import com.campinglog.campinglogbackserver.board.exception.NotLikedError;
import com.campinglog.campinglogbackserver.board.exception.NotYourBoardError;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.campinglog.campinglogbackserver.board.controller")
@Slf4j
public class BoardRestControllerExceptionHandler {

    private ResponseEntity<Map<String, String>> body(HttpStatus s, String code, String msg) {
        Map<String, String> m = new HashMap<>();
        m.put("error", code);
        m.put("message", msg);
        m.put("status", String.valueOf(s.value()));
        return ResponseEntity.status(s).body(m);
    }

    // 404
    @ExceptionHandler({BoardNotFoundError.class, CommentNotFoundError.class,
        EntityNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException e) {
        log.error("Board NotFound: {}", e.getMessage());
        return body(HttpStatus.NOT_FOUND, "BOARD_NOT_FOUND", e.getMessage());
    }

    // 400
    @ExceptionHandler({InvalidBoardRequestError.class, IllegalArgumentException.class,
        NotLikedError.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException e) {
        log.error("Board BadRequest: {}", e.getMessage());
        String code = (e instanceof NotLikedError) ? "NOT_LIKED" : "INVALID_REQUEST";
        return body(HttpStatus.BAD_REQUEST, code, e.getMessage());
    }

    // 403
    @ExceptionHandler(NotYourBoardError.class)
    public ResponseEntity<Map<String, String>> handleForbidden(NotYourBoardError e) {
        log.error("Board Forbidden: {}", e.getMessage());
        return body(HttpStatus.FORBIDDEN, "FORBIDDEN", e.getMessage());
    }

    // 409
    @ExceptionHandler(AlreadyLikedError.class)
    public ResponseEntity<Map<String, String>> handleConflict(AlreadyLikedError e) {
        log.error("Board Conflict: {}", e.getMessage());
        return body(HttpStatus.CONFLICT, "ALREADY_LIKED", e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException e) {
        log.error("Board RuntimeException: ", e);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
            "게시판 서비스에 오류가 발생했습니다.");
    }
}

