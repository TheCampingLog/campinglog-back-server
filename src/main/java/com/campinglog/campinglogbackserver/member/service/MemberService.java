package com.campinglog.campinglogbackserver.member.service;

import com.campinglog.campinglogbackserver.member.dto.request.RequestAddMember;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMember;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberBoards;

public interface MemberService {

  public void addMember(RequestAddMember requestAddMember);
  ResponseGetMember getMemberByEmail(String email);
  ResponseGetMemberBoards getMyBoards(String email, int pageNo);
}
