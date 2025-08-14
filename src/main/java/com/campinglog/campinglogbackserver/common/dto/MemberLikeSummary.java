package com.campinglog.campinglogbackserver.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberLikeSummary {
    private String memberId;
    private Long totalLikes;
}
