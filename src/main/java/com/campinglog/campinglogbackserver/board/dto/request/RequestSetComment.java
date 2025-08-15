package com.campinglog.campinglogbackserver.board.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestSetComment {

    @NotBlank(message = "게시글 ID는 필수입니다.")
    private String boardId;

    @NotBlank(message = "댓글 ID는 필수입니다.")
    private String commentId;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(min = 1, max = 1000, message = "댓글은 1자 이상 1000자 이하여야 합니다.")
    private String content;


}
