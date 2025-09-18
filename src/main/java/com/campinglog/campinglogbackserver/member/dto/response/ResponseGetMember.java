package com.campinglog.campinglogbackserver.member.dto.response;

import com.campinglog.campinglogbackserver.member.entity.Member.MemberGrade;
import com.campinglog.campinglogbackserver.member.entity.Member.Role;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ResponseGetMember {
  private String email;
  private String name;
  private String nickname;
  private LocalDate birthday;
  private String phoneNumber;
  private String profileImage;
  private Role role;
  private MemberGrade memberGrade;
  private LocalDate joinDate;
}
