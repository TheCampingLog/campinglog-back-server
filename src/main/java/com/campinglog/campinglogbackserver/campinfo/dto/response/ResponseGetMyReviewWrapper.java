package com.campinglog.campinglogbackserver.campinfo.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResponseGetMyReviewWrapper {
  private List<ResponseGetMyReview> content;
  private int page;
  private int size;
  private long totalElements;
  private int totalPage;
  private boolean hasNext;
}
