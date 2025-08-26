package com.campinglog.campinglogbackserver.board.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResponseGetCommentsWrapper {
    private List<ResponseGetComments> content;
    private int totalPages;
    private int totalComments;
    private int pageNumber;
    private int pageSize;
    private boolean isFirst;
    private boolean isLast;

}
