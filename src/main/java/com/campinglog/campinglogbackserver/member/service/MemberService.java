package com.campinglog.campinglogbackserver.member.service;

import com.campinglog.campinglogbackserver.member.dto.request.RequestAddMember;
import com.campinglog.campinglogbackserver.member.dto.request.RequestVerifyPassword;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMember;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberBoardList;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberProfileImage;

public interface MemberService {

  public void addMember(RequestAddMember requestAddMember);
  ResponseGetMember getMemberByEmail(String email);
  ResponseGetMemberBoardList getMyBoards(String email, int pageNo);
  ResponseGetMemberProfileImage getProfileImage(String email);
  void verifyPassword(String email, RequestVerifyPassword request);

  void assertEmailAvailable(String email);
  void assertNicknameAvailable(String nickname);
}
