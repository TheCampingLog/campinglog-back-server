package com.campinglog.campinglogbackserver.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseGetLike {
    private String boardId;
    private int likeCount;

}
