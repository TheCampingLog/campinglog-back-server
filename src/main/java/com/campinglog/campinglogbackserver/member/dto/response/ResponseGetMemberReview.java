package com.campinglog.campinglogbackserver.member.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ResponseGetMemberReview {
    private String reviewContent; //review
    private double reviewScore; //review
    private String facltNm;     //외부 api
    private String firstImageUrl; //외부 api
    private String mapX;
    private String mapY;
    private LocalDateTime createAt;
    private long id;
}
