package com.campinglog.campinglogbackserver.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberLikeSummary {
    private String memberId;
    private Long totalLikes;
}
