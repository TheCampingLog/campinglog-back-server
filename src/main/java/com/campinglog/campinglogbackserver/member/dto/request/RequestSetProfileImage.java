package com.campinglog.campinglogbackserver.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestSetProfileImage {
    //@NotBlank(message = "profileImage(URL)는 필수입니다.")
    private String profileImage;
}
