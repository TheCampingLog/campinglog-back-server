package com.campinglog.campinglogbackserver.campinfo.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestGetBoardReview {
  private String mapX;
  private String mapY;
}
