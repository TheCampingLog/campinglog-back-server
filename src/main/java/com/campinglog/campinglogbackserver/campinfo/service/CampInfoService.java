package com.campinglog.campinglogbackserver.campinfo.service;

import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampByKeyword;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampDetail;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampListLatest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CampInfoService {
    Mono<List<ResponseGetCampListLatest>> getCampListLatest(int pageNo);
    Mono<ResponseGetCampDetail> getCampDetail(String mapX, String mapY);
    Mono<List<ResponseGetCampByKeyword>> getCampByKeyword(String keyword, int pageNo);
}
