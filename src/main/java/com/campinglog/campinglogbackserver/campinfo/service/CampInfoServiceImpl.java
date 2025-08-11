package com.campinglog.campinglogbackserver.campinfo.service;

import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestAddReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestRemoveReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestSetReview;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetBoardReview;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetBoardReviewRank;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampByKeyword;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampDetail;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampListLatest;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetReviewList;
import com.campinglog.campinglogbackserver.campinfo.entity.ReviewOfBoard;
import com.campinglog.campinglogbackserver.campinfo.entity.Review;
import com.campinglog.campinglogbackserver.campinfo.repository.ReviewOfBoardRepository;
import com.campinglog.campinglogbackserver.campinfo.repository.ReviewRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class CampInfoServiceImpl implements CampInfoService{

    private final ReviewOfBoardRepository reviewOfBoardRepository;
    private final ReviewRepository reviewRepository;
    private final WebClient campWebClient;
    @Value("${external.camp.api-key}")
    private String serviceKey;
    private final ObjectMapper objectMapper;

    @Override
    public ResponseGetBoardReview getBoardReview(String mapX, String mapY) {
        ReviewOfBoard reviewOfBoard = reviewOfBoardRepository.findByMapXAndMapY(mapX, mapY);
        return ResponseGetBoardReview.builder()
            .reviewAverage(reviewOfBoard.getReviewAverage())
            .reviewCount(reviewOfBoard.getReviewCount())
            .build();
    }

    @Override
    public Mono<List<ResponseGetBoardReviewRank>> getBoardReviewRank(int limit) {
        Pageable pageable = PageRequest.of(
            0,
            limit,
            Sort.by(Sort.Direction.DESC, "reviewAverage")
                .and(Sort.by(Sort.Direction.DESC, "id"))
        );

//        List<ResponseGetBoardReviewRank> ranks = reviewOfBoardRepository.findAllByReviewAverageIsNotNull(pageable)
//            .stream()
//            .map(rank -> ResponseGetBoardReviewRank.builder()
//                .reviewAverage(rank.getReviewAverage())
//                .mapX(rank.getMapX())
//                .mapY(rank.getMapY())
//                .build())
//            .toList();
//
//        for (ResponseGetBoardReviewRank rank : ranks) {
//            Mono<ResponseGetCampDetail> result = getCampDetail(rank.getMapX(), rank.getMapY());
//            rank.setDoNm()
//        }

        return Mono.fromCallable(() ->
            reviewOfBoardRepository.findAllByReviewAverageIsNotNull(pageable)
                .stream()
                .map(rank -> ResponseGetBoardReviewRank.builder()
                    .reviewAverage(rank.getReviewAverage())
                    .mapY(rank.getMapY())
                    .mapX(rank.getMapX())
                    .build())
                .toList()
        )
            .subscribeOn(Schedulers.boundedElastic()) // JPA 호출 격리
            .flatMapMany(Flux::fromIterable)
            .flatMapSequential(rank ->    //순서 보장
                getCampDetail(rank.getMapX(), rank.getMapY())
                    .map(detail -> {
                        if(detail != null) {
                            rank.setDoNm(detail.getDoNm());
                            rank.setSigunguNm(detail.getSigunguNm());
                            rank.setFirstImageUrl(detail.getFirstImageUrl());
                        }
                        return rank;
                    })
                    .defaultIfEmpty(rank)
            )
            .collectList();

    }

    @Override
    public void addReview(RequestAddReview requestAddReview) {
        Review review = Review.builder()
            .email(requestAddReview.getEmail())
            .mapX(requestAddReview.getMapX())
            .mapY(requestAddReview.getMapY())
            .reviewContent(requestAddReview.getReviewContent())
            .reviewScore(requestAddReview.getReviewScore())
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
        Optional<Review> optionalReview = reviewRepository.findById(Review.builder().Id(requestSetReview.getId()).build().getId());

        boolean update = false;

        if(optionalReview.isPresent()) {
            Review review = optionalReview.get();

            if(!review.getReviewContent().equals(requestSetReview.getNewReviewContent())) {
                review.setReviewContent(requestSetReview.getNewReviewContent());
                update = true;
            }

            if(!review.getReviewScore().equals(requestSetReview.getNewReviewScore())) {
                double oldScore = review.getReviewScore();
                review.setReviewScore(requestSetReview.getNewReviewScore());
                update = true;

                ReviewOfBoard reviewOfBoard = reviewOfBoardRepository.findByMapXAndMapY(review.getMapX(), review.getMapY());
                reviewOfBoard.setReviewAverage(((reviewOfBoard.getReviewAverage()*reviewOfBoard.getReviewCount()) + (requestSetReview.getNewReviewScore()-oldScore))
                    / reviewOfBoard.getReviewCount());
                reviewOfBoardRepository.save(reviewOfBoard);
            }

            if(update) {
                reviewRepository.save(review);
            }
        }

    }

    @Override
    public void removeReview(RequestRemoveReview requestRemoveReview) {
        Review review = Review.builder().Id(requestRemoveReview.getId()).build();
        Optional<Review> deleteReview = reviewRepository.findById(review.getId());
        reviewRepository.deleteById(review.getId());

        ReviewOfBoard reviewOfBoard = reviewOfBoardRepository.findByMapXAndMapY(deleteReview.get().getMapX(), deleteReview.get().getMapY());
        reviewOfBoard.setReviewAverage((reviewOfBoard.getReviewAverage()*reviewOfBoard.getReviewCount()-deleteReview.get().getReviewScore()) / (reviewOfBoard.getReviewCount()-1));
        reviewOfBoard.setReviewCount(reviewOfBoard.getReviewCount()-1);
        reviewOfBoardRepository.save(reviewOfBoard);
    }

    @Override
    public List<ResponseGetReviewList> getReviewList(String mapX, String mapY) {
        List<ResponseGetReviewList> list = new ArrayList<>();
        List<Review> reviews = reviewRepository.findByMapXAndMapY(mapX, mapY);
        for(Review review : reviews) {
            ResponseGetReviewList reviewUnit = ResponseGetReviewList.builder()
                .reviewImage(review.getReviewImage())
                .reviewContent(review.getReviewContent())
                .reviewScore(review.getReviewScore())
                .email(review.getEmail())
                .postAt(review.getPostAt())
                .setAt(review.getSetAt())
                .build();
            list.add(reviewUnit);
        }
        return list;
    }

    @Override
    public Mono<ResponseGetCampDetail> getCampDetail(String mapX, String mapY) {
        return campWebClient.get()
            .uri(uri -> uri.path("/locationBasedList")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "CampingLog")
                .queryParam("_type", "json")
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 4)
                .queryParam("mapX", mapX)
                .queryParam("mapY", mapY)
                .queryParam("radius", 100)
                .build())
            .retrieve()
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(6))
            .map(json -> {
                List<ResponseGetCampDetail> list = parseItems(json, ResponseGetCampDetail.class);
                if(list.isEmpty()) return null;
                return list.get(0);
            });
    } //eternalApiException

    @Override
    public Mono<List<ResponseGetCampListLatest>> getCampListLatest(int pageNo) {
        return campWebClient.get()
            .uri(uri -> uri.path("/basedList")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "CampingLog")
                .queryParam("_type", "json")
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", 4)
                .build())
            .retrieve()
//                .onStatus(HttpStatusCode::is4xxClientError, r ->
//                        r.bodyToMono(String.class)
//                                .flatMap(b -> Mono.error(new ExternalApiException("400: "+b))))
//                .onStatus(HttpStatusCode::is5xxServerError, r ->
//                        r.bodyToMono(String.class)
//                                .flatMap(b -> Mono.error(new ExternalApiException("500: " + b))))
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(6))
            .map(json -> {
                int total = parseTotalCount(json);
                List<ResponseGetCampListLatest> list = parseItems(json, ResponseGetCampListLatest.class);
                list.forEach(item -> item.setTotalCount(total));
                return list;
            });
    }

    @Override
    public Mono<List<ResponseGetCampByKeyword>> getCampByKeyword(String keyword, int pageNo) {
        return campWebClient.get()
            .uri(uri -> uri.path("/searchList")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "CampingLog")
                .queryParam("_type", "json")
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", 4)
                .queryParam("keyword", keyword)
                .build())
            .retrieve()
//                .onStatus(HttpStatusCode::is4xxClientError, r ->
//                        r.bodyToMono(String.class)
//                                .flatMap(b -> Mono.error(new ExternalApiException("400: "+b))))
//                .onStatus(HttpStatusCode::is5xxServerError, r ->
//                        r.bodyToMono(String.class)
//                                .flatMap(b -> Mono.error(new ExternalApiException("500: " + b))))
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(6))
            .map(json -> {
                int total = parseTotalCount(json);
                List<ResponseGetCampByKeyword> list = parseItems(json, ResponseGetCampByKeyword.class);
                list.forEach(item -> item.setTotalCount(total));
                return list;
            });
    }


    //try catch -> advice
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
            throw new RuntimeException("GoCamping JSON 파싱 실패", e);
        }
    }

    //try catch -> advice
    private int parseTotalCount(String json) {
        try{
            JsonNode root = objectMapper.readTree(json);
            return root.path("response").path("body").path("totalCount").asInt(0);
        } catch (Exception e) {
            throw new RuntimeException("GoCamping totalCount 파싱 실패", e);
        }
    }
}
