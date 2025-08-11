package com.campinglog.campinglogbackserver.member.service;

import com.campinglog.campinglogbackserver.member.dto.request.RequestAddMember;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetUser;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.exception.MemberCreationError;
import com.campinglog.campinglogbackserver.member.exception.MemberNotFoundError;
import com.campinglog.campinglogbackserver.member.repository.MemberRespository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

  private final MemberRespository memberRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final ModelMapper modelMapper;                    // 주입받아 재사용

  @Override
  public void addMember(RequestAddMember requestAddMember) {
    Member member = new ModelMapper().map(requestAddMember, Member.class);
    String encodedPassword = bCryptPasswordEncoder.encode(requestAddMember.getPassword());

    member.setPassword(encodedPassword);

    try {
      memberRepository.save(member);
    } catch (RuntimeException e) {
      throw new MemberCreationError("회원 가입에 실패했습니다");
    }
  }

  @Override
  @Transactional
  public ResponseGetUser getMemberByEmail(String email) {
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));
    return modelMapper.map(member, ResponseGetUser.class);
  }
}
