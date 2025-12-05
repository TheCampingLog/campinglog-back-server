package com.campinglog.campinglogbackserver.campinfo.service;

import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestAddReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestRemoveReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestSetReview;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetBoardReview;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetBoardReviewRankList;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampByKeywordList;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampDetail;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampLatestList;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampWrapper;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetMyReviewWrapper;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetReviewListWrapper;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CampInfoServiceCF {
  CompletableFuture<ResponseGetCampWrapper<ResponseGetCampLatestList>> getCampListLatest(int pageNo, int size);
  CompletableFuture<ResponseGetCampDetail> getCampDetail(String mapX, String mapY);
  CompletableFuture<ResponseGetCampWrapper<ResponseGetCampByKeywordList>> getCampByKeyword(String keyword, int pageNo, int size);
  void addReview(RequestAddReview requestAddReview);
  ResponseGetBoardReview getBoardReview(String mapX, String mapY);
  void setReview(RequestSetReview requestSetReview);
  void removeReview(RequestRemoveReview requestRemoveReview);
  ResponseGetReviewListWrapper getReviewList(String mapX, String mapY, int pageNo, int size);
  CompletableFuture<List<ResponseGetBoardReviewRankList>> getBoardReviewRank(int limit);
  CompletableFuture<ResponseGetMyReviewWrapper> getMyReviews(String email, int pageNo, int size);
}
