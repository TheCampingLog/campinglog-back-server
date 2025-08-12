package com.campinglog.campinglogbackserver.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseGetMemberBoard {
    private String title;
    private String content;
    private String boardImage;
    private Date createdAt;
    private String boardId;
}
