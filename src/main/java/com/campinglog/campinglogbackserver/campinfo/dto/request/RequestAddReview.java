package com.campinglog.campinglogbackserver.campinfo.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestAddReview {
  private String mapX;
  private String mapY;

  private String email;

  private String reviewContent;
  private String reviewScore;
}
