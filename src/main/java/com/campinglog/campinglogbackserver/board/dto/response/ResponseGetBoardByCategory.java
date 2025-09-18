package com.campinglog.campinglogbackserver.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseGetBoardByCategory {

    private String category;
    private String boardId;
    private String title;
    private String content;
    private String categoryName;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private String boardImage;
    private String createdAt;
    private String nickName;

}
