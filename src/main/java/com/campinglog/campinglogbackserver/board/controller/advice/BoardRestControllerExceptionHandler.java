package com.campinglog.campinglogbackserver.board.controller.advice;

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

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFound(EntityNotFoundException e) {
        log.error("Board EntityNotFoundException: {}", e.getMessage());

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "BOARD_NOT_FOUND");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("status", "404");

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        log.error("Board IllegalArgumentException: {}", e.getMessage());

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "INVALID_REQUEST");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("status", "400");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        log.error("Board RuntimeException: {}", e.getMessage());

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "INTERNAL_SERVER_ERROR");
        errorResponse.put("message", "게시판 서비스에 오류가 발생했습니다.");
        errorResponse.put("status", "500");

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

