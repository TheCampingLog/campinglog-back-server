package com.campinglog.campinglogbackserver.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestChangePassword {

    private String currentPassword;
    @Size(min = 8, max = 100)
    @NotBlank(message = "password는 필수입니다.")
    private String newPassword;
}
