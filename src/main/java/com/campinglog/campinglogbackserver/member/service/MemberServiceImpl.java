package com.campinglog.campinglogbackserver.member.service;

import com.campinglog.campinglogbackserver.board.entity.Board;
import com.campinglog.campinglogbackserver.board.repository.BoardRepository;
import com.campinglog.campinglogbackserver.member.dto.MemberLikeSummary;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    // 1. 중복 이메일 체크
    if (memberRepository.existsByEmail(requestAddMember.getEmail())) {
      throw new DuplicateEmailError("이미 존재하는 이메일입니다: " + requestAddMember.getEmail());
    }

    // 2. 중복 닉네임 체크
    if (memberRepository.existsByNickname(requestAddMember.getNickname())) {
      throw new DuplicateNicknameError("이미 존재하는 닉네임입니다: " + requestAddMember.getNickname());
    }

    // 3. DTO → 엔티티 매핑
    // 주입 받은 modelMapper 사용
    Member member = modelMapper.map(requestAddMember, Member.class);
    member.setPassword(bCryptPasswordEncoder.encode(requestAddMember.getPassword()));

    // save 시 발생하는 예외는 JPA DataAccessException/RuntimeException
    // → 별도로 감싸지 않고 스프링 전역 예외 처리(@RestControllerAdvice)에서 대응
    memberRepository.save(member);
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
    ResponseGetMemberProfileImage resp = modelMapper.map(member, ResponseGetMemberProfileImage.class);
    if (resp.getProfileImage() == null) {
      throw new ProfileImageNotFoundError("등록된 프로필 이미지가 없습니다.");
    }
    return resp;
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

  @Override
  public List<Map<String, Object>> updateRankWeekly(int memberNo) {
    // 이번 주 구간: [이번 주 목요일 0시, 다음 주 목요일 0시)
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    LocalDate thisThursday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.THURSDAY));
    LocalDate nextThursday = thisThursday.plusWeeks(1);

    LocalDateTime start = thisThursday.atStartOfDay();
    LocalDateTime end   = nextThursday.atStartOfDay();

    // 좋아요 발생 시점 기준 집계
    List<MemberRepository.WeeklyLikeAggRow> rows = memberRepository.findTopMembersByLikeCreatedAt(start, end);

    // Top N만 추리기
    List<Map<String,Object>> result = new ArrayList<>();
    IntStream.range(0, Math.min(memberNo, rows.size()))
            .forEach(i -> {
              MemberRepository.WeeklyLikeAggRow r = rows.get(i);
              Map<String,Object> item = new LinkedHashMap<>();
              item.put("rank", i+1);
              item.put("email", r.getEmail());
              item.put("nickname", r.getNickname());
              item.put("profileImage", r.getProfileImage());
              item.put("totalLikes", r.getTotalLikes());
              result.add(item);
            });

    return result;
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

    if (!bCryptPasswordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
      throw new PasswordMismatchError("현재 비밀번호가 일치하지 않습니다.");
    }
    if (bCryptPasswordEncoder.matches(request.getNewPassword(), member.getPassword())) {
      throw new InvalidPasswordError("새 비밀번호가 기존 비밀번호와 동일합니다.");
    }

    member.setPassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
    memberRepository.save(member);
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

    memberRepository.save(member);
  }

  @Override
  @Transactional
  public void addProfileImage(String email, RequestSetProfileImage request) {
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));
    member.setProfileImage(request.getProfileImage());
    memberRepository.save(member);
  }

  @Override
  @Transactional
  public void setProfileImage(String email, RequestSetProfileImage request) {
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));
    member.setProfileImage(request.getProfileImage());
    memberRepository.save(member);
  }

  @Override
  @Transactional
  public void deleteMember(String email) {
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));
    memberRepository.delete(member);
  }

  @Override
  @Transactional
  public void deleteProfileImage(String email) {
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));

    if (member.getProfileImage() == null) {
      throw new ProfileImageNotFoundError("프로필 이미지가 존재하지 않습니다. email=" + email);
    }

    member.setProfileImage(null);
    memberRepository.save(member);
  }
}
