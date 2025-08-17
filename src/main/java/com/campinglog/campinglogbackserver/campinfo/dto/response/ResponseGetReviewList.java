package com.campinglog.campinglogbackserver.campinfo.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseGetReviewList {

  private String email;
  private String nickname;

  private String reviewContent;
  private double reviewScore;
  private String reviewImage;

  private LocalDateTime createAt;
  private LocalDateTime updateAt;

}
