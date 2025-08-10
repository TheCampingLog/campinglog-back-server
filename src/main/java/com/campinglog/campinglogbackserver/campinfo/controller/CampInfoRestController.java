package com.campinglog.campinglogbackserver.campinfo.controller;

import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampByKeyword;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampDetail;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampListLatest;
import com.campinglog.campinglogbackserver.campinfo.service.CampInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/camp-info")
public class CampInfoRestController {
    private final CampInfoService campInfoService;

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        return ResponseEntity.ok((Map.of("ok", "ok")));
    }

    @GetMapping("/camps/{pageNo}")
    public ResponseEntity<Mono<List<ResponseGetCampListLatest>>> getCampListLatest(@PathVariable int pageNo) {

        return ResponseEntity.ok(campInfoService.getCampListLatest(pageNo));
    }

    @GetMapping("/camp/{mapX}/{mapY}")
    public ResponseEntity<Mono<ResponseGetCampDetail>> getCampDetail(@PathVariable String mapX, @PathVariable String mapY) {
        return ResponseEntity.ok(campInfoService.getCampDetail(mapX, mapY));
    }

    @GetMapping("/camp/keyword/{keyword}/{pageNo}")
    public ResponseEntity<Mono<List<ResponseGetCampByKeyword>>> getCampByKeyword(@PathVariable String keyword, @PathVariable int pageNo) {
        return ResponseEntity.ok(campInfoService.getCampByKeyword(keyword, pageNo));
    }


}
