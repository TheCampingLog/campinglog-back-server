package com.campinglog.campinglogbackserver.campinfo.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestGetBoardReview {
  @DecimalMin(value = "124.0", message = "124.0~132.0 범위 내에서 입력해 주세요.")
  @DecimalMax(value = "132.0", message = "124.0~132.0 범위 내에서 입력해 주세요.")
  private String mapX;
  @DecimalMin(value = "33.0", message = "124.0~132.0 범위 내에서 입력해 주세요.")
  @DecimalMax(value = "38.7", message = "124.0~132.0 범위 내에서 입력해 주세요.")
  private String mapY;
}
