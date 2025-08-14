package com.campinglog.campinglogbackserver.member.service;

import com.campinglog.campinglogbackserver.board.entity.Board;
import com.campinglog.campinglogbackserver.board.repository.BoardRepository;
import com.campinglog.campinglogbackserver.common.dto.MemberLikeSummary;
import com.campinglog.campinglogbackserver.member.dto.request.*;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMember;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberBoard;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberBoardList;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberProfileImage;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.exception.*;
import com.campinglog.campinglogbackserver.member.repository.MemberRepository; // ← 오타 수정
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ← Spring 트랜잭션

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

  private final MemberRepository memberRepository;          // ← 이름 수정
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final ModelMapper modelMapper;                    // 주입받아 재사용
  private final BoardRepository boardRepository;            // @OneToMany 미사용
  private static final int PAGE_SIZE = 4;

  @Override
  public void addMember(RequestAddMember requestAddMember) {
    // 주입 받은 modelMapper 사용
    Member member = modelMapper.map(requestAddMember, Member.class);
    String encodedPassword = bCryptPasswordEncoder.encode(requestAddMember.getPassword());
    member.setPassword(encodedPassword);

    try {
      memberRepository.save(member);
    } catch (RuntimeException e) {
      throw new MemberCreationError("회원 가입에 실패했습니다");
    }
  }

  @Override
  @Transactional(readOnly = true)
  public ResponseGetMember getMember(String email) {
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));
    return modelMapper.map(member, ResponseGetMember.class);
  }

  @Override
  @Transactional(readOnly = true)
  public ResponseGetMemberBoardList getBoards(String email, int pageNo) {
    int pageIndex = Math.max(pageNo - 1, 0); // 1-based → 0-based
    PageRequest pageable = PageRequest.of(pageIndex, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));

    Page<Board> page = boardRepository.findByMemberEmail(email, pageable);

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
  @Transactional(readOnly = true)
  public ResponseGetMemberProfileImage getProfileImage(String email) {
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));
    return modelMapper.map(member, ResponseGetMemberProfileImage.class);
  }

  @Override
  @Transactional(readOnly = true)
  public void verifyPassword(String email, RequestVerifyPassword request) {
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));

    boolean matched = bCryptPasswordEncoder.matches(request.getPassword(), member.getPassword());
    if (!matched) {
      throw new PasswordMismatchError("비밀번호가 일치하지 않습니다.");
    }
  }

  @Override
  @Transactional
  public int updateGradeWeekly() {
    List<MemberLikeSummary> rows = boardRepository.sumLikesGroupByMember();

    if (rows.isEmpty()) {
      log.info("No boards found. No grade changes.");
      return 0;
    }

    // 필요하면 DTO로 변환
    List<MemberLikeSummary> totals = rows.stream()
            .map(p -> new MemberLikeSummary(p.getMemberId(),
                    p.getTotalLikes() == null ? 0L : p.getTotalLikes()))
            .toList();

    // PK = email(String)
    List<String> memberIds = totals.stream().map(MemberLikeSummary::getMemberId).toList();
    Map<String, Member> memberMap = memberRepository.findAllById(memberIds).stream()
            .collect(Collectors.toMap(Member::getEmail, m -> m));

    List<Member> changedMembers = new ArrayList<>();
    for (MemberLikeSummary t : totals) {
      Member m = memberMap.get(t.getMemberId());
      if (m == null) continue;
      Member.MemberGrade newGrade = decideByLikes(t.getTotalLikes());
      if (m.getMemberGrade() != newGrade) {
        m.setMemberGrade(newGrade);
        changedMembers.add(m);
      }
    }
    if (!changedMembers.isEmpty()) memberRepository.saveAll(changedMembers);
    log.info("Weekly promotion executed. changed={}", changedMembers.size());
    return changedMembers.size();
  }

  /** 등급 정책: GREEN < BLUE < RED < BLACK  (임계값: 20/50/100) */
  private Member.MemberGrade decideByLikes(long totalLikes) {
    if (totalLikes >= 100) return Member.MemberGrade.BLACK;
    if (totalLikes >= 50)  return Member.MemberGrade.RED;
    if (totalLikes >= 20)  return Member.MemberGrade.BLUE;
    return Member.MemberGrade.GREEN;
  }

  @Override
  @Transactional(readOnly = true)
  public void checkEmailAvailable(String email) {
    if (memberRepository.existsByEmail(email)) {
      throw new DuplicateEmailError("이미 사용 중인 이메일입니다.");
    }
  }

  @Override
  @Transactional(readOnly = true)
  public void checkNicknameAvailable(String nickname) {
    if (memberRepository.existsByNickname(nickname)) {
      throw new DuplicateNicknameError("이미 사용 중인 닉네임입니다.");
    }
  }

  @Override
  @Transactional
  public void setPassword(String email, RequestChangePassword request) {
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));

    boolean matches = bCryptPasswordEncoder.matches(request.getCurrentPassword(), member.getPassword());
    if (!matches) {
      throw new PasswordMismatchError("현재 비밀번호가 일치하지 않습니다.");
    }
    if (bCryptPasswordEncoder.matches(request.getNewPassword(), member.getPassword())) {
      throw new IllegalArgumentException("새 비밀번호가 기존 비밀번호와 동일합니다.");
    }

    member.setPassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
    try {
      memberRepository.save(member);
    } catch (RuntimeException e) {
      throw new MemberCreationError("비밀번호 변경에 실패했습니다");
    }
  }

  @Override
  @Transactional
  public void setMember(String email, RequestUpdateMember request) {
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

    // 3) 널 스킵 매핑
    boolean prevSkip = modelMapper.getConfiguration().isSkipNullEnabled();
    modelMapper.getConfiguration().setSkipNullEnabled(true);
    try {
      modelMapper.map(request, member);
    } finally {
      modelMapper.getConfiguration().setSkipNullEnabled(prevSkip);
    }

    try {
      memberRepository.save(member);
    } catch (RuntimeException e) {
      throw new MemberCreationError("회원 정보 수정에 실패했습니다");
    }
  }

  @Override
  @Transactional
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
  @Transactional
  public void setProfileImage(String email, RequestSetProfileImage request) {
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));
    member.setProfileImage(request.getProfileImage());
    try {
      memberRepository.save(member);
    } catch (RuntimeException e) {
      throw new MemberCreationError("프로필 이미지 수정에 실패했습니다.");
    }
  }

  @Override
  @Transactional
  public void deleteMember(String email) {
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));

    try {
      memberRepository.delete(member);
    } catch (RuntimeException e) {
      throw new MemberCreationError("회원 탈퇴에 실패했습니다.");
    }
  }
}
