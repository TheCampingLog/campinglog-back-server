package com.campinglog.campinglogbackserver.campinfo.controller;

import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestAddReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestRemoveReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestSetReview;
import com.campinglog.campinglogbackserver.campinfo.dto.response.*;
import com.campinglog.campinglogbackserver.campinfo.service.CampInfoServiceCF;
import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/camps")
public class CampInfoRestController {
    private final CampInfoServiceCF campInfoServiceCF;

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        return ResponseEntity.ok((Map.of("ok", "ok")));
    }

    @GetMapping("/list")
    public CompletableFuture<ResponseEntity<ResponseGetCampWrapper<ResponseGetCampLatestList>>> getCampListLatest(
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "4") int size) {
      return campInfoServiceCF.getCampListLatest(pageNo, size).thenApply(ResponseEntity::ok);
    }

    @GetMapping("/detail/{mapX}/{mapY}")
    public CompletableFuture<ResponseEntity<ResponseGetCampDetail>> getCampDetail(
        @PathVariable String mapX,
        @PathVariable String mapY) {
      return campInfoServiceCF.getCampDetail(mapX, mapY).thenApply(ResponseEntity::ok);
    }

    @GetMapping("/keyword")
    public CompletableFuture<ResponseEntity<ResponseGetCampWrapper<ResponseGetCampByKeywordList>>> getCampByKeyword(@RequestParam String keyword, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "4") int size) {
        return campInfoServiceCF.getCampByKeyword(keyword, pageNo, size).thenApply(ResponseEntity::ok);
    }

    @PostMapping("/members/reviews")
    public ResponseEntity<Map<String, String>> addReview(@AuthenticationPrincipal String email, @Valid @RequestBody RequestAddReview requestAddReview) {
        requestAddReview.setEmail(email);
      campInfoServiceCF.addReview(requestAddReview);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/members/reviews")
    public ResponseEntity<Map<String, String>> setReview(@Valid @RequestBody RequestSetReview requestSetReview) {
      campInfoServiceCF.setReview(requestSetReview);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/members/reviews")
    public  ResponseEntity<Map<String, String>> removeReview(@Valid @RequestBody RequestRemoveReview requestRemoveReview) {
      campInfoServiceCF.removeReview(requestRemoveReview);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/reviews/{mapX}/{mapY}")
    public ResponseEntity<ResponseGetReviewListWrapper> getReviewList(@PathVariable String mapX, @PathVariable String mapY,
                                                                      @RequestParam(defaultValue = "0") int pageNo, @RequestParam(defaultValue = "4") int size) {
        return ResponseEntity.ok(campInfoServiceCF.getReviewList(mapX, mapY, pageNo, size));
    }

    @GetMapping("/reviews/board/{mapX}/{mapY}")
    public ResponseEntity<ResponseGetBoardReview> getBoardReview(@PathVariable String mapX, @PathVariable String mapY) {
        return ResponseEntity.ok(campInfoServiceCF.getBoardReview(mapX, mapY));
    }

    @GetMapping("/reviews/board/rank")
    public CompletableFuture<ResponseEntity<List<ResponseGetBoardReviewRankList>>> getBoardReviewRank(@RequestParam(value = "limit", defaultValue = "3") int limit) {
        return campInfoServiceCF.getBoardReviewRank(limit).thenApply(ResponseEntity::ok);
    }
}
