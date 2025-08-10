package com.campinglog.campinglogbackserver.campinfo.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseGetBoardReview {
  private Double reviewEverage;
  private String reviewCount;
}
