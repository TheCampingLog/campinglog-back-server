package com.campinglog.campinglogbackserver.campinfo.controller.advice;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomErrorResponse {
  private String message;
  private String path;
  private LocalDateTime timestamp;
}
