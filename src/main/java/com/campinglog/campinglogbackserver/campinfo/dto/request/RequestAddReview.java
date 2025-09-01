package com.campinglog.campinglogbackserver.campinfo.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestAddReview {
  private String mapX;
  private String mapY;

  private String email;

  @NotBlank(message = "리뷰 내용을 입력해 주세요.")
  @Size(max = 500)
  private String reviewContent;

  @NotNull
  @DecimalMin(value = "0.5", message = "별점은 0.5 이상이어야 합니다.")
  @DecimalMax(value = "5.0", message = "별점은 5.0 이하여야 합니다.")
  private Double reviewScore;


  private String reviewImage;
}
