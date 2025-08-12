package com.campinglog.campinglogbackserver.member.service;

import com.campinglog.campinglogbackserver.board.entity.Board;
import com.campinglog.campinglogbackserver.board.repository.BoardRepository;
import com.campinglog.campinglogbackserver.member.dto.request.*;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMember;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberBoard;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberBoardList;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberProfileImage;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.exception.*;
import com.campinglog.campinglogbackserver.member.repository.MemberRespository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

  private final MemberRespository memberRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final ModelMapper modelMapper;                    // 주입받아 재사용
  private static final int PAGE_SIZE = 4;
  private final BoardRepository boardRepository; //@OneToMany 사용하지 않고

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
  public ResponseGetMember getMemberByEmail(String email) {
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));
    return modelMapper.map(member, ResponseGetMember.class);
  }

  @Override
  public ResponseGetMemberBoardList getMyBoards(String email, int pageNo) {
    int pageIndex = Math.max(pageNo - 1, 0); // 1-based → 0-based
    PageRequest pageable = PageRequest.of(pageIndex, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));

    Page<Board> page = boardRepository.findByEmail(email, pageable);

    List<ResponseGetMemberBoard> items = page.getContent().stream()
            .map(board -> modelMapper.map(board, ResponseGetMemberBoard.class))
            .collect(Collectors.toList());

    return ResponseGetMemberBoardList.builder()
            .items(items)
            .page(pageNo)
            .size(PAGE_SIZE)
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .build();
  }

  @Override
  public ResponseGetMemberProfileImage getProfileImage(String email) {
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));
    return modelMapper.map(member, ResponseGetMemberProfileImage.class);
  }

  @Override
  public void verifyPassword(String email, RequestVerifyPassword request) {
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));

    boolean matched = bCryptPasswordEncoder.matches(request.getPassword(), member.getPassword());
    if (!matched) {
      throw new PasswordMismatchError("비밀번호가 일치하지 않습니다.");
    }
  }

  @Override
  public void assertEmailAvailable(String email) {
    if (memberRepository.existsByEmail(email)) {
      throw new DuplicateEmailError("이미 사용 중인 이메일입니다.");
    }
  }

  @Override
  public void assertNicknameAvailable(String nickname) {
    if (memberRepository.existsByNickname(nickname)) {
      throw new DuplicateNicknameError("이미 사용 중인 닉네임입니다.");
    }
  }

  @Override
  @Transactional
  public void changePassword(String email, RequestChangePassword request) {
    // 1) 회원 조회
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));
    boolean matches = bCryptPasswordEncoder.matches(request.getCurrentPassword(), member.getPassword());
    if (!matches) {
      throw new PasswordMismatchError("현재 비밀번호가 일치하지 않습니다.");
    }
    // 3) 새 비밀번호가 기존과 동일한지 방어 (선택)
    if (bCryptPasswordEncoder.matches(request.getNewPassword(), member.getPassword())) {
      throw new IllegalArgumentException("새 비밀번호가 기존 비밀번호와 동일합니다.");
    }
    // 4) 변경 및 저장
    member.setPassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
    try {
      memberRepository.save(member);
    } catch (RuntimeException e) {
      throw new MemberCreationError("비밀번호 변경에 실패했습니다");
    }
  }

  @Override
  public void updateMember(String email, RequestUpdateMember request) {
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));

    // 1) 닉네임 변경 시: 자기 자신 제외 중복 체크
    if (request.getNickname() != null && !request.getNickname().equals(member.getNickname())) {
      if (memberRepository.existsByNicknameAndEmailNot(request.getNickname(), email)) {
        throw new DuplicateNicknameError("이미 사용 중인 닉네임입니다.");
      }
      member.setNickname(request.getNickname());
    }

    // 2) 전화번호 변경 시: 자기 자신 제외 중복 체크
    if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(member.getPhoneNumber())) {
      if (memberRepository.existsByPhoneNumberAndEmailNot(request.getPhoneNumber(), email)) {
        throw new DuplicatePhoneNumberError("이미 사용 중인 전화번호입니다.");
      }
      member.setPhoneNumber(request.getPhoneNumber());
    }

    // 3) 그 외 필드 널 스킵 매핑
    boolean prevSkip = modelMapper.getConfiguration().isSkipNullEnabled();
    modelMapper.getConfiguration().setSkipNullEnabled(true);
    try {
      modelMapper.map(request, member); // null인 값은 덮어쓰지 않음
    } finally {
      modelMapper.getConfiguration().setSkipNullEnabled(prevSkip);
    }

    // 4) 저장 (실패 시 래핑)
    try {
      memberRepository.save(member);
    } catch (RuntimeException e) {
      throw new MemberCreationError("회원 정보 수정에 실패했습니다");
    }
  }

  @Override
  public void addProfileImage(String email, RequestSetProfileImage request) {
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));
    member.setProfileImage(request.getProfileImage());
    try {
      memberRepository.save(member);
    } catch (RuntimeException e) {
      throw new MemberCreationError("프로필 이미지 등록에 실패했습니다");
    }
  }

  @Override
  public void updateProfileImage(String email, RequestSetProfileImage request) {
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));
    member.setProfileImage(request.getProfileImage());
    try {
      memberRepository.save(member);
    } catch (RuntimeException e) {
      throw new MemberCreationError("프로필 이미지 수정에 실패했습니다.");
    }
  }

}
