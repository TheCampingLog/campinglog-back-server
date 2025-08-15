package com.campinglog.campinglogbackserver.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseGetBoardDetail {
    private String boardId;
    private String boardImage;
    private String title;
    private String content;
    private String nickname;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private String createdAt;
    private String email;

}
