package com.campinglog.campinglogbackserver.campinfo.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResponseGetMyReview {
  private String reviewContent; //review
  private double reviewScore; //review
  private String facltNm;     //외부 api
  private String firstImageUrl; //외부 api
  private String mapX;
  private String mapY;
  private LocalDateTime createAt;
  private long id;
}
