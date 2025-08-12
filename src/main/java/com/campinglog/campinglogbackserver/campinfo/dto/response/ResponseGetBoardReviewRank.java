package com.campinglog.campinglogbackserver.campinfo.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseGetBoardReviewRank {
  private double reviewAverage;
  private String firstImageUrl;
  private String doNm;
  private String sigunguNm;
  private String mapX;
  private String mapY;
  private String facltNm;
}
