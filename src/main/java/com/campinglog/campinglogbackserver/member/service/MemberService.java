package com.campinglog.campinglogbackserver.member.service;

import com.campinglog.campinglogbackserver.member.dto.request.RequestAddMember;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetUser;

public interface MemberService {

  public void addMember(RequestAddMember requestAddMember);
  ResponseGetUser getMemberByEmail(String email);
}
