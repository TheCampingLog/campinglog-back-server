package com.campinglog.campinglogbackserver.member.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestUpdateMember {
    //@Size(min = 2, max = 20, message = "이름은 2~20자여야 합니다.")
    private String name;
    //@Size(min = 3, max = 16, message = "닉네임은 3~16자여야 합니다.")
    private String nickname;
    //@Pattern(regexp = "^[0-9\\-]{9,15}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phoneNumber;
    private String profileImage;
}
