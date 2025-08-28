package com.campinglog.campinglogbackserver.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseGetMemberActivity {
    private String email;
    private long boardCount;
    private long commentCount;
    private long reviewCount;
    private long likeCount;
}
