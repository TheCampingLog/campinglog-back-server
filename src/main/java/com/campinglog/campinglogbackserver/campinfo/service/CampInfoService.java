package com.campinglog.campinglogbackserver.campinfo.service;

import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestAddReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestGetBoardReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestRemoveReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestSetReview;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetBoardReview;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetBoardReviewRank;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampByKeyword;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampDetail;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampListLatest;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetReviewList;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CampInfoService {
    Mono<List<ResponseGetCampListLatest>> getCampListLatest(int pageNo);
    Mono<ResponseGetCampDetail> getCampDetail(String mapX, String mapY);
    Mono<List<ResponseGetCampByKeyword>> getCampByKeyword(String keyword, int pageNo);
    void addReview(RequestAddReview requestAddReview);
    ResponseGetBoardReview getBoardReview(String mapX, String mapY);
    void setReview(RequestSetReview requestSetReview);
    void removeReview(RequestRemoveReview requestRemoveReview);
    List<ResponseGetReviewList> getReviewList(String mapX, String mapY);
    Mono<List<ResponseGetBoardReviewRank>> getBoardReviewRank(int limit);
}
