package com.campinglog.campinglogbackserver.account.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestAddMember {

  @NotNull
  @Size(max = 100)
  String email;
  @NotNull
  @Size(min = 8, max = 100)
  String password;
  @NotNull
  @Size(max = 50)
  String name;
  @NotNull
  @Size(max = 50)
  String nickname;
  @JsonFormat(pattern = "yyyy-MM-dd")
  @NotNull
  LocalDate birthday;
  @NotNull
  @Size(max = 20)
  String phoneNumber;

}
