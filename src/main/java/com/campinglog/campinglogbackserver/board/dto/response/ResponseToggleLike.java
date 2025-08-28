package com.campinglog.campinglogbackserver.board.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseToggleLike {

    private boolean isLiked;
    private int likeCount;

}
