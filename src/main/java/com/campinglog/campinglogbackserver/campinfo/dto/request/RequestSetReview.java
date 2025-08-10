package com.campinglog.campinglogbackserver.campinfo.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestSetReview {
  private Long id;
//  private String mapX;
//  private String mapY;
//
//  private String email;

  private String newReviewContent;
  private String newReviewScore;

}
