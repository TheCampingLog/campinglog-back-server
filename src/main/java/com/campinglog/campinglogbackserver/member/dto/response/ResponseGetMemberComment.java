package com.campinglog.campinglogbackserver.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseGetMemberComment {
    private String commentId;
    private String content;
    private String nickname;
    private String createdAt;
    private String boardId;
}
