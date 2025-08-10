package com.campinglog.campinglogbackserver.campinfo.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestGetBoardReview {
  private String mapX;
  private String mapY;
}
