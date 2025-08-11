package com.campinglog.campinglogbackserver.campinfo.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseGetReviewList {

  private String email;
  private String nickname;

  private String reviewContent;
  private String reviewScore;
  private String reviewImage;

}
