package com.campinglog.campinglogbackserver.campinfo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Builder
@Data
public class ResponseGetCampListLatest {
    private String facltNm;
    private String doNm;
    private String sigunguNm;
    private String addr1;
    private String addr2;
    private String mapX;
    private String mapY;
    private String tel;
    private String sbrsCl;
    private String firstImageUrl;
    private int totalCount;
}
