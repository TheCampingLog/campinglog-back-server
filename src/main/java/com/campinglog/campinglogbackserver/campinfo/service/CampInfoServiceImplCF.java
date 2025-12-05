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
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetMyReviewList;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetMyReviewWrapper;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetReviewList;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetReviewListWrapper;
import com.campinglog.campinglogbackserver.campinfo.entity.Review;
import com.campinglog.campinglogbackserver.campinfo.entity.ReviewOfBoard;
import com.campinglog.campinglogbackserver.campinfo.exception.ApiParsingError;
import com.campinglog.campinglogbackserver.campinfo.exception.InvalidLimitError;
import com.campinglog.campinglogbackserver.campinfo.exception.NoExistCampError;
import com.campinglog.campinglogbackserver.campinfo.exception.NoSearchResultError;
import com.campinglog.campinglogbackserver.campinfo.exception.NullReviewError;
import com.campinglog.campinglogbackserver.campinfo.repository.ReviewOfBoardRepository;
import com.campinglog.campinglogbackserver.campinfo.repository.ReviewRepository;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.exception.MemberNotFoundError;
import com.campinglog.campinglogbackserver.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CampInfoServiceImplCF implements CampInfoServiceCF{

  private final ReviewOfBoardRepository reviewOfBoardRepository;
  private final ReviewRepository reviewRepository;
  private final MemberRepository memberRepository;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  @Value("${external.camp.base-url}")
  private String baseUrl;
  @Value("${external.camp.api-key}")
  private String serviceKey;

  @Override
  public CompletableFuture<ResponseGetCampWrapper<ResponseGetCampLatestList>> getCampListLatest(int pageNo, int size) {
    return CompletableFuture.supplyAsync(() -> {
          // RestTemplate URL 구성
          String url = String.format(
              "%s/basedList?serviceKey=%s&MobileOS=ETC&MobileApp=CampingLog&_type=json&pageNo=%d&numOfRows=%d",
              baseUrl, serviceKey, pageNo, size
          );
          // 동기 호출
          return restTemplate.getForObject(url, String.class);
        })
        .thenApply(json -> {
          // JSON 파싱
          int total = parseTotalCount(json);
          List<ResponseGetCampLatestList> list = parseItems(json, ResponseGetCampLatestList.class);

          return ResponseGetCampWrapper.<ResponseGetCampLatestList>builder()
              .totalCount(total)
              .items(list)
              .totalPage((total + size - 1) / size)
              .hasNext((total + size - 1) / size > pageNo)
              .page(pageNo)
              .size(size)
              .build();
        }).orTimeout(6, TimeUnit.SECONDS);
  }

  @Override
  public CompletableFuture<ResponseGetCampDetail> getCampDetail(String mapX, String mapY) {
    return CompletableFuture.supplyAsync(() -> {
      // RestTemplate URL 구성
      String url = String.format(
          "%s/locationBasedList?serviceKey=%s&MobileOS=ETC&MobileApp=CampingLog&_type=json&pageNo=1&numOfRows=4&mapX=%s&mapY=%s&radius=100",
          baseUrl, serviceKey, mapX, mapY
      );

      // 동기 호출
      String json = restTemplate.getForObject(url, String.class);

      // JSON 파싱
      List<ResponseGetCampDetail> list = parseItems(json, ResponseGetCampDetail.class);
      if (list.isEmpty()) {
        throw new NoExistCampError("해당 게시글이 존재하지 않습니다.");
      }
      return list.get(0);
    });
  }

  @Override
  public CompletableFuture<ResponseGetCampWrapper<ResponseGetCampByKeywordList>> getCampByKeyword(String keyword, int pageNo, int size) {
    return CompletableFuture.supplyAsync(() -> {
          // RestTemplate URL 구성
          String url = String.format(
              "%s/searchList?serviceKey=%s&MobileOS=ETC&MobileApp=CampingLog&_type=json&pageNo=%d&numOfRows=%d&keyword=%s",
              baseUrl, serviceKey, pageNo, size, keyword
          );
          return restTemplate.getForObject(url, String.class);
        })
        .thenApply(json -> {
          // JSON 파싱
          int total = parseTotalCount(json);
          List<ResponseGetCampByKeywordList> list = parseItems(json, ResponseGetCampByKeywordList.class);

          if (list.isEmpty()) {
            throw new NoSearchResultError("검색 결과가 없습니다: keyword=" + keyword);
          }

          return ResponseGetCampWrapper.<ResponseGetCampByKeywordList>builder()
              .totalCount(total)
              .items(list)
              .totalPage((total + size - 1) / size) // 페이지 올림
              .hasNext((total + size - 1) / size > pageNo)
              .page(pageNo)
              .size(size)
              .build();
        });
  }

  @Transactional
  @Override
  public void addReview(RequestAddReview requestAddReview) {
    if(!memberRepository.existsByEmail(requestAddReview.getEmail())) {
      throw new MemberNotFoundError("회원 없음: email= " + requestAddReview.getEmail());
    }
    Member memberRef = memberRepository.getReferenceById(requestAddReview.getEmail());
    Review review = Review.builder()
        .member(memberRef)
        .mapX(requestAddReview.getMapX())
        .mapY(requestAddReview.getMapY())
        .reviewContent(requestAddReview.getReviewContent())
        .reviewScore(requestAddReview.getReviewScore())
        .reviewImage(requestAddReview.getReviewImage())
        .build();
    reviewRepository.save(review);

    if(reviewOfBoardRepository.existsByMapXAndMapY(review.getMapX(), review.getMapY())) {
      ReviewOfBoard reviewOfBoard = reviewOfBoardRepository.findByMapXAndMapY(review.getMapX(), review.getMapY());
      int reviewCount = reviewOfBoard.getReviewCount();
      reviewOfBoard.setReviewCount(reviewCount + 1);
      reviewOfBoard.setReviewAverage(
          ((reviewOfBoard.getReviewAverage() * reviewCount) + review.getReviewScore())
              / reviewOfBoard.getReviewCount());
      reviewOfBoardRepository.save(reviewOfBoard);
    } else {
      ReviewOfBoard reviewOfBoard = ReviewOfBoard.builder()
          .reviewCount(1)
          .reviewAverage(requestAddReview.getReviewScore())
          .mapX(requestAddReview.getMapX())
          .mapY(requestAddReview.getMapY())
          .build();
      reviewOfBoardRepository.save(reviewOfBoard);
    }
  }

  @Override
  public void setReview(RequestSetReview requestSetReview) {
    Review review = reviewRepository.findById(Review.builder().id(requestSetReview.getId()).build().getId())
        .orElseThrow(() -> new NullReviewError("리뷰 없음: id = " + requestSetReview.getId()));

    boolean update = false;

    if(!Objects.equals(review.getReviewContent(), requestSetReview.getNewReviewContent())) {
      review.setReviewContent(requestSetReview.getNewReviewContent());
      update = true;
    }

    if(!Objects.equals(review.getReviewScore(), requestSetReview.getNewReviewScore())) {
      double oldScore = review.getReviewScore();
      review.setReviewScore(requestSetReview.getNewReviewScore());
      update = true;

      ReviewOfBoard reviewOfBoard = reviewOfBoardRepository.findByMapXAndMapY(review.getMapX(), review.getMapY());
      reviewOfBoard.setReviewAverage(((reviewOfBoard.getReviewAverage()*reviewOfBoard.getReviewCount()) + (requestSetReview.getNewReviewScore()-oldScore))
          / reviewOfBoard.getReviewCount());
      reviewOfBoardRepository.save(reviewOfBoard);
    }

    if(!Objects.equals(review.getReviewImage(),requestSetReview.getNewReviewImage())) {
      review.setReviewImage(requestSetReview.getNewReviewImage());
      update = true;
    }

    if(update) {
      reviewRepository.save(review);
    }
  }

  @Transactional
  @Override
  public void removeReview(RequestRemoveReview requestRemoveReview) {
    Review review = Review.builder().id(requestRemoveReview.getId()).build();
    Review deleteReview = reviewRepository.findById(review.getId())
        .orElseThrow(() -> new NullReviewError("삭제할 리뷰 없음: id = " + requestRemoveReview.getId()));
    reviewRepository.deleteById(review.getId());

    ReviewOfBoard reviewOfBoard = reviewOfBoardRepository.findByMapXAndMapY(deleteReview.getMapX(), deleteReview.getMapY());
    reviewOfBoard.setReviewAverage((reviewOfBoard.getReviewAverage()*reviewOfBoard.getReviewCount()-deleteReview.getReviewScore()) / (reviewOfBoard.getReviewCount()-1));
    reviewOfBoard.setReviewCount(reviewOfBoard.getReviewCount()-1);
    reviewOfBoardRepository.save(reviewOfBoard);
    ReviewOfBoard checkReviewOfBoard = reviewOfBoardRepository.findByMapXAndMapY(deleteReview.getMapX(), deleteReview.getMapY());
    if(checkReviewOfBoard.getReviewCount()==0) {
      reviewOfBoardRepository.deleteByMapXAndMapY(deleteReview.getMapX(), deleteReview.getMapY());
    }
  }

  @Override
  public ResponseGetReviewListWrapper getReviewList(String mapX, String mapY, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));
    Page<Review> reviews = reviewRepository.findByMapXAndMapY(mapX, mapY, pageable);

    List<ResponseGetReviewList> content = reviews.getContent().stream()
        .map(review -> ResponseGetReviewList.builder()
            .reviewImage(review.getReviewImage())
            .reviewContent(review.getReviewContent())
            .reviewScore(review.getReviewScore())
            .email(review.getMember().getEmail())
            .updateAt(review.getUpdateAt())
            .nickname(review.getMember().getNickname())
            .createAt(review.getCreateAt())
            .build())
        .toList();

    return ResponseGetReviewListWrapper.builder()
        .items(content)
        .page(reviews.getNumber())
        .size(reviews.getSize())
        .hasNext(reviews.hasNext())
        .totalElement(reviews.getTotalElements())
        .totalPages(reviews.getTotalPages())
        .build();

  }

  @Override
  public ResponseGetBoardReview getBoardReview(String mapX, String mapY) {
    ReviewOfBoard reviewOfBoard = reviewOfBoardRepository.findByMapXAndMapY(mapX, mapY);
    if(reviewOfBoard == null) {
      return ResponseGetBoardReview.builder()
          .reviewAverage(0.0)
          .reviewCount(0)
          .build();
    }
    return ResponseGetBoardReview.builder()
        .reviewAverage(reviewOfBoard.getReviewAverage())
        .reviewCount(reviewOfBoard.getReviewCount())
        .build();
  }

  @Override
  public CompletableFuture<List<ResponseGetBoardReviewRankList>> getBoardReviewRank(int limit) {
    if (limit <= 0) {
      throw new InvalidLimitError("리뷰 랭킹 조회 시 limit은 0보다 커야 합니다.");
    }

    Pageable pageable = PageRequest.of(
        0,
        limit,
        Sort.by(Sort.Direction.DESC, "reviewAverage")
            .and(Sort.by(Sort.Direction.DESC, "id"))
    );

    // 1. DB 조회를 비동기로 실행
    return CompletableFuture.supplyAsync(() ->
        reviewOfBoardRepository.findAllByReviewAverageIsNotNull(pageable)
            .stream()
            .map(rank -> ResponseGetBoardReviewRankList.builder()
                .reviewAverage(rank.getReviewAverage())
                .mapY(rank.getMapY())
                .mapX(rank.getMapX())
                .build())
            .toList()
    ).thenCompose(rankList -> {
      // 2. 각 rank마다 RestTemplate로 캠핑장 상세 조회
      List<CompletableFuture<ResponseGetBoardReviewRankList>> futureList = rankList.stream()
          .map(rank -> getCampDetail(rank.getMapX(), rank.getMapY())
              .thenApply(detail -> { // detail이 완료되면 처리
                if (detail != null) {
                  rank.setDoNm(detail.getDoNm());
                  rank.setSigunguNm(detail.getSigunguNm());
                  rank.setFirstImageUrl(detail.getFirstImageUrl());
                  rank.setFacltNm(detail.getFacltNm());
                }
                return rank;
              })
              .exceptionally(e -> rank)).toList();

      // 3. 모든 CompletableFuture 완료 후 리스트로 합치기
      CompletableFuture<Void> allDone = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
      return allDone.thenApply(v -> futureList.stream()
          .map(CompletableFuture::join)
          .toList()
      );
    });
  }

  @Override
  public CompletableFuture<ResponseGetMyReviewWrapper> getMyReviews(String email, int pageNo, int size) {
    Pageable pageable = PageRequest.of(pageNo - 1, size, Sort.by(Direction.DESC, "createAt"));

    // 1. DB 조회를 비동기로 실행
    return CompletableFuture.supplyAsync(() -> reviewRepository.findByMember_Email(email, pageable))
        .thenCompose(page -> {
          List<CompletableFuture<ResponseGetMyReviewList>> futureList = page.getContent().stream()
              .map(review -> getCampDetail(review.getMapX(), review.getMapY()) // CompletableFuture<ResponseGetCampDetail>
                  .thenApply(detail -> ResponseGetMyReviewList.builder()
                      .id(review.getId())
                      .reviewScore(review.getReviewScore())
                      .reviewContent(review.getReviewContent())
                      .mapX(review.getMapX())
                      .mapY(review.getMapY())
                      .createAt(review.getCreateAt())
                      .facltNm(detail != null ? detail.getFacltNm() : null)
                      .firstImageUrl(detail != null ? detail.getFirstImageUrl() : null)
                      .build()
                  )
                  .exceptionally(e -> ResponseGetMyReviewList.builder()
                      .id(review.getId())
                      .reviewScore(review.getReviewScore())
                      .reviewContent(review.getReviewContent())
                      .mapX(review.getMapX())
                      .mapY(review.getMapY())
                      .createAt(review.getCreateAt())
                      .facltNm(null)
                      .firstImageUrl(null)
                      .build()
                  )
              ).toList();

          // 2. 모든 CompletableFuture 완료 후 리스트로 합치기
          CompletableFuture<Void> allDone = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
          return allDone.thenApply(v -> {
            List<ResponseGetMyReviewList> reviewList = futureList.stream()
                .map(CompletableFuture::join)
                .toList();

            return ResponseGetMyReviewWrapper.builder()
                .content(reviewList)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPage(page.getTotalPages())
                .hasNext(page.hasNext())
                .build();
          });
        });
  }

  private <T> List<T> parseItems(String json, Class<T> type) {
    try {
      JsonNode root = objectMapper.readTree(json);
      JsonNode items = root.path("response").path("body").path("items").path("item");

      List<T> result = new ArrayList<>();
      if (items.isArray()) {
        for (JsonNode item : items) {
          result.add(objectMapper.treeToValue(item, type));
        }
      } else if (items.isObject()) {
        result.add(objectMapper.treeToValue(items, type));
      }
      return result;
    } catch (Exception e) {
      throw new ApiParsingError("GoCamping JSON 파싱 실패");
    }
  }

  private int parseTotalCount(String json) {
    try{
      JsonNode root = objectMapper.readTree(json);
      return root.path("response").path("body").path("totalCount").asInt(0);
    } catch (Exception e) {
      throw new ApiParsingError("GoCamping totalCount 파싱 실패");
    }
  }
}
