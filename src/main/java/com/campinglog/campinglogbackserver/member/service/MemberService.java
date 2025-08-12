package com.campinglog.campinglogbackserver.member.service;

import com.campinglog.campinglogbackserver.member.dto.request.*;
import com.campinglog.campinglogbackserver.member.dto.request.RequestAddMember;
import com.campinglog.campinglogbackserver.member.dto.request.RequestChangePassword;
import com.campinglog.campinglogbackserver.member.dto.request.RequestUpdateMember;
import com.campinglog.campinglogbackserver.member.dto.request.RequestVerifyPassword;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMember;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberBoardList;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberProfileImage;
import jakarta.validation.Valid;

public interface MemberService {

  public void addMember(RequestAddMember requestAddMember);
  public void addProfileImage(String email, @Valid RequestSetProfileImage request);

  public ResponseGetMember getMember(String email);
  public ResponseGetMemberBoardList getBoards(String email, int pageNo);
  public ResponseGetMemberProfileImage getProfileImage(String email);

  public void setPassword(String email, @Valid RequestChangePassword request);
  public void setMember(String email, @Valid RequestUpdateMember request);
  public void setProfileImage(String email, @Valid RequestSetProfileImage request);

  public void deleteMember(String email);

  public void checkEmailAvailable(String email);
  public void checkNicknameAvailable(String nickname);
  public void verifyPassword(String email, RequestVerifyPassword request);
}
