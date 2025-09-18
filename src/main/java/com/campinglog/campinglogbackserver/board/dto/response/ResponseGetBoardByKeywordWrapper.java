package com.campinglog.campinglogbackserver.board.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResponseGetBoardByKeywordWrapper {
    private List<ResponseGetBoardByKeyword> content;
    private int totalPages;
    private long totalElements;
    private int pageNumber;
    private int pageSize;
    private boolean isFirst;
    private boolean isLast;

}
