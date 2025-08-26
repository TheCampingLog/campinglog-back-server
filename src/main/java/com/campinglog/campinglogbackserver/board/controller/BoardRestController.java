package com.campinglog.campinglogbackserver.board.controller;

import com.campinglog.campinglogbackserver.board.dto.request.RequestAddBoard;
import com.campinglog.campinglogbackserver.board.dto.request.RequestAddComment;
import com.campinglog.campinglogbackserver.board.dto.request.RequestAddLike;
import com.campinglog.campinglogbackserver.board.dto.request.RequestSetBoard;
import com.campinglog.campinglogbackserver.board.dto.request.RequestSetComment;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardByCategory;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardByCategoryWrapper;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardByKeyword;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardDetail;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardRank;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetComments;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetCommentsWrapper;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetLike;
import com.campinglog.campinglogbackserver.board.entity.Board;
import com.campinglog.campinglogbackserver.board.entity.BoardLike;
import com.campinglog.campinglogbackserver.board.entity.Comment;
import com.campinglog.campinglogbackserver.board.service.BoardService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
@Validated
public class BoardRestController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<Map<String, String>> addBoard(
        @AuthenticationPrincipal String email,
        @Valid @RequestBody RequestAddBoard requestAddBoard) {
        requestAddBoard.setEmail(email);
        Board saved = boardService.addBoard(requestAddBoard);
        Map<String, String> response = new HashMap<>();
        response.put("message", "게시글이 등록되었습니다.");
        response.put("boardId", saved.getBoardId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<Map<String, String>> setBoard(
        @AuthenticationPrincipal String email, @PathVariable String boardId,
        @Valid @RequestBody RequestSetBoard requestsetBoard) {
        requestsetBoard.setBoardId(boardId);
        requestsetBoard.setEmail(email);
        boardService.setBoard(requestsetBoard);
        Map<String, String> response = new HashMap<>();
        response.put("message", "게시글이 수정되었습니다.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<Map<String, String>> deleteBoard(@PathVariable String boardId) {
        boardService.deleteBoard(boardId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rank")
    public ResponseEntity<List<ResponseGetBoardRank>> getBoardRank(
        @RequestParam(value = "limit", defaultValue = "3") int limit) {
        List<ResponseGetBoardRank> result = boardService.getBoardRank(limit);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<ResponseGetBoardDetail> getBoardDetail(@PathVariable String boardId) {
        ResponseGetBoardDetail result = boardService.getBoardDetail(boardId);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/search")
    public ResponseEntity<List<ResponseGetBoardByKeyword>> searchBoards(
        @RequestParam String keyword, @RequestParam(required = false, defaultValue = "1") int page,
        @RequestParam(required = false, defaultValue = "3") int size) {
        List<ResponseGetBoardByKeyword> result = boardService.searchBoards(keyword, page, size);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{boardId}/comment")
    public ResponseEntity<Map<String, String>> addComment(
        @AuthenticationPrincipal String email, @PathVariable String boardId,
        @Valid @RequestBody RequestAddComment requestAddComment) {
        requestAddComment.setBoardId(boardId);
        requestAddComment.setEmail(email);
        Comment saved = boardService.addComment(boardId, requestAddComment);
        Map<String, String> response = new HashMap<>();
        response.put("message", "댓글이 등록되었습니다.");
        response.put("boardId", boardId);
        response.put("commentId", saved.getCommentId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/category")
    public ResponseEntity<ResponseGetBoardByCategoryWrapper> getBoardsByCategory(
        @RequestParam String category, @RequestParam(required = false, defaultValue = "1") int page,
        @RequestParam(required = false, defaultValue = "3") int size) {
        ResponseGetBoardByCategoryWrapper result = boardService.getBoardsByCategory(category, page,
            size);
        return ResponseEntity.ok(result);

    }

    @GetMapping("/{boardId}/comments")
    public ResponseEntity<ResponseGetCommentsWrapper> getComments(@PathVariable String boardId,
        @RequestParam(required = false, defaultValue = "1") int page,
        @RequestParam(required = false, defaultValue = "3") int size) {

        log.info("[Controller] getComments 호출됨. page={}, size={}", page, size);
        ResponseGetCommentsWrapper result = boardService.getComments(boardId, page, size);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{boardId}/comments/{commentId}")
    public ResponseEntity<Map<String, String>> updateComment(@PathVariable String boardId,
        @PathVariable String commentId, @Valid @RequestBody RequestSetComment requestSetComment) {
        requestSetComment.setBoardId(boardId);
        requestSetComment.setCommentId(commentId);
        boardService.updateComment(boardId, commentId, requestSetComment);

        Map<String, String> response = new HashMap<>();
        response.put("message", "댓글이 수정되었습니다.");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{boardId}/comments/{commentId}")
    public ResponseEntity<Map<String, String>> deleteComment(@PathVariable String boardId,
        @PathVariable String commentId) {
        boardService.deleteComment(boardId, commentId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "댓글이 삭제되었습니다.");
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{boardId}/likes")
    public ResponseEntity<Map<String, String>> addLike(
        @AuthenticationPrincipal String email, @PathVariable String boardId,
        @Valid @RequestBody RequestAddLike requestAddLike) {
        requestAddLike.setEmail(email);
        BoardLike saved = boardService.addLike(boardId, requestAddLike);
        Map<String, String> response = new HashMap<>();
        response.put("message", "좋아요가 추가되었습니다.");
        response.put("boarId", boardId);
        response.put("likeId", saved.getLikeId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{boardId}/likes")
    public ResponseEntity<ResponseGetLike> getLikes(@PathVariable String boardId) {
        ResponseGetLike result = boardService.getLikes(boardId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{boardId}/likes")
    public ResponseEntity<Map<String, String>> deleteLike(
        @AuthenticationPrincipal String email, @PathVariable String boardId) {
        boardService.deleteLike(boardId, email);
        Map<String, String> response = new HashMap<>();
        response.put("message", "좋아요가 취소되었습니다.");
        return ResponseEntity.ok(response);
    }


}
