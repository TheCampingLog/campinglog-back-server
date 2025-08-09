package com.campinglog.campinglogbackserver.account.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestLogin {

  @NotNull
  @Size(max = 100)
  String email;
  @NotNull
  @Size(min = 8, max = 100)
  String password;

}
