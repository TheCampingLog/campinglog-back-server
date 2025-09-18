package com.campinglog.campinglogbackserver.campinfo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ResponseGetCampWrapper<T> {
    private List<T> items;
    private int page;
    private int size;
    private int totalCount;
    private int totalPage;
    private boolean hasNext;
}
