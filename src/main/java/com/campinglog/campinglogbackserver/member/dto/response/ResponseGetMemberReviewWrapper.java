package com.campinglog.campinglogbackserver.member.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ResponseGetMemberReviewWrapper {
    private List<ResponseGetMemberReview> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPage;
    private boolean hasNext;
}
