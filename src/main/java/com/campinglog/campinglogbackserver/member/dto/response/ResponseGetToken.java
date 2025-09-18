package com.campinglog.campinglogbackserver.member.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResponseGetToken {

  String jwtToken;
  String refreshToken;
}
