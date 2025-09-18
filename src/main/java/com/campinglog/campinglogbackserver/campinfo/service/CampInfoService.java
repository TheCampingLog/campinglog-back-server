package com.campinglog.campinglogbackserver.campinfo.service;

import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestAddReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestRemoveReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestSetReview;
import com.campinglog.campinglogbackserver.campinfo.dto.response.*;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CampInfoService {
    Mono<ResponseGetCampWrapper<ResponseGetCampLatestList>> getCampListLatest(int pageNo, int size);
    Mono<ResponseGetCampDetail> getCampDetail(String mapX, String mapY);
    Mono<ResponseGetCampWrapper<ResponseGetCampByKeywordList>> getCampByKeyword(String keyword, int pageNo, int size);
    void addReview(RequestAddReview requestAddReview);
    ResponseGetBoardReview getBoardReview(String mapX, String mapY);
    void setReview(RequestSetReview requestSetReview);
    void removeReview(RequestRemoveReview requestRemoveReview);
    ResponseGetReviewListWrapper getReviewList(String mapX, String mapY, int pageNo, int size);
    Mono<List<ResponseGetBoardReviewRankList>> getBoardReviewRank(int limit);
    Mono<ResponseGetMyReviewWrapper> getMyReviews(String email, int pageNo, int size);
}
