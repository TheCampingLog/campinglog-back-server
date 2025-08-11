package com.campinglog.campinglogbackserver.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseGetBoardRank {

    private String boardId;
    private String boardImage;
    private String title;
    private String nickname;
    private int rank;
    private int viewCount;

}
