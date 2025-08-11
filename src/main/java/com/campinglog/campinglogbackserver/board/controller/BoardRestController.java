package com.campinglog.campinglogbackserver.board.controller;

import com.campinglog.campinglogbackserver.board.dto.request.RequestAddBoard;
import com.campinglog.campinglogbackserver.board.dto.request.RequestSetBoard;
import com.campinglog.campinglogbackserver.board.dto.response.ResMessage;
import com.campinglog.campinglogbackserver.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BoardRestController {

    private final BoardService boardService;


    @PostMapping("/boards")
    public ResponseEntity<ResMessage> addBoard(@RequestBody RequestAddBoard requestAddBoard) {
        boardService.addBoard(requestAddBoard);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/boards/{boardId}")
    public ResponseEntity<ResMessage> setBoard(@PathVariable String boardId,
        @RequestBody RequestSetBoard requestsetBoard) {
        requestsetBoard.setBoardId(boardId);
        boardService.setBoard(requestsetBoard);
        return ResponseEntity.ok(new ResMessage("success"));
    }
}
