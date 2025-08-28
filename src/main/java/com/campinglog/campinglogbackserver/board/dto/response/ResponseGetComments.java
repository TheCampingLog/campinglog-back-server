package com.campinglog.campinglogbackserver.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseGetComments {
    private String commentId;
    private String content;
    private String nickname;
    private String createdAt;
    private String email;

}

