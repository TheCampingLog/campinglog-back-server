package com.campinglog.campinglogbackserver.campinfo.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseGetReviewListWrapper {
  private List<ResponseGetReviewList> items;
  private int page;
  private int size;
  private boolean hasNext;
  private long totalElement;
  private int totalPages;
}
