package com.campinglog.campinglogbackserver.account.service;

import com.campinglog.campinglogbackserver.account.dto.request.RequestAddMember;
import com.campinglog.campinglogbackserver.account.entity.Member;
import com.campinglog.campinglogbackserver.account.exception.MemberCreationError;
import com.campinglog.campinglogbackserver.account.repository.MemberRespository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

  private final MemberRespository memberRespository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  @Override
  public void addMember(RequestAddMember requestAddMember) {
    Member member = new ModelMapper().map(requestAddMember, Member.class);
    String encodedPassword = bCryptPasswordEncoder.encode(requestAddMember.getPassword());

    member.setPassword(encodedPassword);

    try {
      memberRespository.save(member);
    } catch (RuntimeException e) {
      throw new MemberCreationError("회원 가입에 실패했습니다");
    }
  }
}
