package com.campinglog.campinglogbackserver.member.service;

import com.campinglog.campinglogbackserver.member.dto.request.*;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMember;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberBoardList;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberProfileImage;
import jakarta.validation.Valid;

public interface MemberService {

  public void addMember(RequestAddMember requestAddMember);
  ResponseGetMember getMemberByEmail(String email);
  ResponseGetMemberBoardList getMyBoards(String email, int pageNo);
  ResponseGetMemberProfileImage getProfileImage(String email);
  void verifyPassword(String email, RequestVerifyPassword request);

  void assertEmailAvailable(String email);
  void assertNicknameAvailable(String nickname);

  void changePassword(String email, @Valid RequestChangePassword request);

  void updateMember(String email, @Valid RequestUpdateMember request);

  void addProfileImage(String email, @Valid RequestSetProfileImage request);

  void updateProfileImage(String email, @Valid RequestSetProfileImage request);

  void deleteMember(String email);

  void updateMemberGrade(String email);
}
