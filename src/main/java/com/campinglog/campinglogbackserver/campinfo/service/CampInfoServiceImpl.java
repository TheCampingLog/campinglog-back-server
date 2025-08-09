package com.campinglog.campinglogbackserver.campinfo.service;

import com.campinglog.campinglogbackserver.campinfo.dto.api.CampApi;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampListLatest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CampInfoServiceImpl implements CampInfoService{

    private final WebClient campWebClient;
    @Value("${external.camp.api-key}")
    private String serviceKey;
    private final ObjectMapper objectMapper;

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
            .map(this::parseCampList);
    }


    //try catch -> advice
    private List<ResponseGetCampListLatest> parseCampList(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            List<ResponseGetCampListLatest> result = new ArrayList<>();
            if (items.isArray()) {
                for (JsonNode item : items) {
                    result.add(parseDto(item));
                }
            } else if (items.isObject()) {
                result.add(parseDto(items));
            }
            return result;
        } catch (Exception e) {
            e.getMessage();
        }
        return null;
    }

    private ResponseGetCampListLatest parseDto(JsonNode item) {
        ResponseGetCampListLatest dto = ResponseGetCampListLatest.builder()
            .facltNm(item.path("facltNm").asText(null))
            .doNm(item.path("doNm").asText(null))
            .sigunguNm(item.path("sigunguNm").asText(null))
            .addr1(item.path("addr1").asText(null))
            .addr2(item.path("addr2").asText(null))
            .tel(item.path("tel").asText(null))
            .sbrsCl(item.path("sbrsCl").asText(null))
            .firstImageUrl(item.path("firstImageUrl").asText(null))
            .mapX(item.path("mapX").asText(null))
            .mapY(item.path("mapY").asText(null))
            .build();
        return dto;
    }
}
