package com.campinglog.campinglogbackserver.member.dto.response;

import java.time.LocalDateTime;
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
    private LocalDateTime createdAt;
    private String boardId;
}
