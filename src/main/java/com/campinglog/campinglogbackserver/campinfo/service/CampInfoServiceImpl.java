package com.campinglog.campinglogbackserver.campinfo.service;

import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestAddReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestGetBoardReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestSetReview;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetBoardReview;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampByKeyword;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampDetail;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampListLatest;
import com.campinglog.campinglogbackserver.campinfo.entity.BoardInfo;
import com.campinglog.campinglogbackserver.campinfo.entity.Review;
import com.campinglog.campinglogbackserver.campinfo.repository.ReviewRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampInfoServiceImpl implements CampInfoService{

    private final ReviewRepository reviewRepository;
    private final WebClient campWebClient;
    @Value("${external.camp.api-key}")
    private String serviceKey;
    private final ObjectMapper objectMapper;

    @Override
    public ResponseGetBoardReview getBoardReview(String mapX, String mapY) {
        return null;
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
                review.setReviewScore(requestSetReview.getNewReviewScore());
                update = true;
            }

            if(update) {
                reviewRepository.save(review);
            }
        }

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
            .map(json -> parseItems(json, ResponseGetCampListLatest.class));
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

    private int parseTotalCount(String json) {
        try{
            JsonNode root = objectMapper.readTree(json);
            return root.path("response").path("body").path("totalCount").asInt(0);
        } catch (Exception e) {
            throw new RuntimeException("GoCamping totalCount 파싱 실패", e);
        }
    }
//
//    private ResponseGetCampListLatest parseDto(JsonNode item) {
//        ResponseGetCampListLatest dto = ResponseGetCampListLatest.builder()
//            .facltNm(item.path("facltNm").asText(null))
//            .doNm(item.path("doNm").asText(null))
//            .sigunguNm(item.path("sigunguNm").asText(null))
//            .addr1(item.path("addr1").asText(null))
//            .addr2(item.path("addr2").asText(null))
//            .tel(item.path("tel").asText(null))
//            .sbrsCl(item.path("sbrsCl").asText(null))
//            .firstImageUrl(item.path("firstImageUrl").asText(null))
//            .mapX(item.path("mapX").asText(null))
//            .mapY(item.path("mapY").asText(null))
//            .build();
//        return dto;
//    }
}
