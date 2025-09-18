package com.campinglog.campinglogbackserver.campinfo.controller;

import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestAddReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestRemoveReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestSetReview;
import com.campinglog.campinglogbackserver.campinfo.dto.response.*;
import com.campinglog.campinglogbackserver.campinfo.service.CampInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/camps")
public class CampInfoRestController {
    private final CampInfoService campInfoService;

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        return ResponseEntity.ok((Map.of("ok", "ok")));
    }

    @GetMapping("/list")
    public ResponseEntity<Mono<ResponseGetCampWrapper<ResponseGetCampLatestList>>> getCampListLatest(@RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "4") int size) {

        return ResponseEntity.ok(campInfoService.getCampListLatest(pageNo, size));
    }

    @GetMapping("/detail/{mapX}/{mapY}")
    public ResponseEntity<Mono<ResponseGetCampDetail>> getCampDetail(@PathVariable String mapX, @PathVariable String mapY) {
        return ResponseEntity.ok(campInfoService.getCampDetail(mapX, mapY));
    }

    @GetMapping("/keyword")
    public ResponseEntity<Mono<ResponseGetCampWrapper<ResponseGetCampByKeywordList>>> getCampByKeyword(@RequestParam String keyword, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "4") int size) {
        return ResponseEntity.ok(campInfoService.getCampByKeyword(keyword, pageNo, size));
    }

    @PostMapping("/members/reviews")
    public ResponseEntity<Map<String, String>> addReview(@AuthenticationPrincipal String email, @Valid @RequestBody RequestAddReview requestAddReview) {
        requestAddReview.setEmail(email);
        campInfoService.addReview(requestAddReview);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/members/reviews")
    public ResponseEntity<Map<String, String>> setReview(@Valid @RequestBody RequestSetReview requestSetReview) {
        campInfoService.setReview(requestSetReview);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/members/reviews")
    public  ResponseEntity<Map<String, String>> removeReview(@Valid @RequestBody RequestRemoveReview requestRemoveReview) {
        campInfoService.removeReview(requestRemoveReview);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/reviews/{mapX}/{mapY}")
    public ResponseEntity<ResponseGetReviewListWrapper> getReviewList(@PathVariable String mapX, @PathVariable String mapY,
                                                                      @RequestParam(defaultValue = "0") int pageNo, @RequestParam(defaultValue = "4") int size) {
        return ResponseEntity.ok(campInfoService.getReviewList(mapX, mapY, pageNo, size));
    }

    @GetMapping("/reviews/board/{mapX}/{mapY}")
    public ResponseEntity<ResponseGetBoardReview> getBoardReview(@PathVariable String mapX, @PathVariable String mapY) {
        return ResponseEntity.ok(campInfoService.getBoardReview(mapX, mapY));
    }

    @GetMapping("/reviews/board/rank")
    public ResponseEntity<Mono<List<ResponseGetBoardReviewRankList>>> getBoardReviewRank(@RequestParam(value = "limit", defaultValue = "3") int limit) {
        return ResponseEntity.ok(campInfoService.getBoardReviewRank(limit));
    }

    @GetMapping("/members/mypage/reviews")
    public ResponseEntity<Mono<ResponseGetMyReviewWrapper>> getMyReviews(@AuthenticationPrincipal String email, @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
        @RequestParam(name = "size", defaultValue = "4") int size) {
        return ResponseEntity.ok(campInfoService.getMyReviews(email, pageNo, size));
    }

}
