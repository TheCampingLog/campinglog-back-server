package com.campinglog.campinglogbackserver.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestVerifyPassword {
    @NotBlank(message = "password는 필수입니다.")
    private String password;
}
