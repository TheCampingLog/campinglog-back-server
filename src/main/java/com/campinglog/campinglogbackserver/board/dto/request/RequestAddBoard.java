package com.campinglog.campinglogbackserver.board.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestAddBoard {

    private String title;
    private String content;
    private String categoryName;
    private String boardImage;
    private String email;
}
