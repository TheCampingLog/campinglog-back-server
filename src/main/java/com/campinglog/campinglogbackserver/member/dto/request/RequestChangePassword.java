package com.campinglog.campinglogbackserver.member.dto.request;

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
    private String newPassword;
}
