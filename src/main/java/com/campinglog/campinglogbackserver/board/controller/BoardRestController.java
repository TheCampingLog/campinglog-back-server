package com.campinglog.campinglogbackserver.board.controller;

import com.campinglog.campinglogbackserver.board.dto.request.RequestAddBoard;
import com.campinglog.campinglogbackserver.board.dto.request.RequestSetBoard;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardByCategory;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardByKeyword;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardRank;
import com.campinglog.campinglogbackserver.board.service.BoardService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BoardRestController {

    private final BoardService boardService;


    @PostMapping("/boards")
    public ResponseEntity<Map<String, String>> addBoard(
        @RequestBody RequestAddBoard requestAddBoard) {
        boardService.addBoard(requestAddBoard);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/boards/{boardId}")
    public ResponseEntity<Map<String, String>> setBoard(@PathVariable String boardId,
        @RequestBody RequestSetBoard requestsetBoard) {
        requestsetBoard.setBoardId(boardId);
        boardService.setBoard(requestsetBoard);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/boards/{boardId}")
    public ResponseEntity<Map<String, String>> deleteBoard(@PathVariable String boardId) {
        boardService.deleteBoard(boardId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/boards/rank")
    public ResponseEntity<List<ResponseGetBoardRank>> getBoardRank(
        @RequestParam(value = "limit", defaultValue = "3") int limit) {
        List<ResponseGetBoardRank> result = boardService.getBoardRank(limit);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/boards/search")
    public ResponseEntity<List<ResponseGetBoardByKeyword>> searchBoards(
        @RequestParam String keyword, @RequestParam(required = false, defaultValue = "1") int page,
        @RequestParam(required = false, defaultValue = "3") int size) {
        List<ResponseGetBoardByKeyword> result = boardService.searchBoards(keyword, page, size);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/boards/category")
    public ResponseEntity<List<ResponseGetBoardByCategory>> getBoardsByCategory(@RequestParam String category, @RequestParam(required = false, defaultValue = "1")int page,
        @RequestParam(required = false, defaultValue = "3")int size){
        List<ResponseGetBoardByCategory> result =boardService.getBoardsByCategory(category, page, size);
        return ResponseEntity.ok(result);
    }


}
