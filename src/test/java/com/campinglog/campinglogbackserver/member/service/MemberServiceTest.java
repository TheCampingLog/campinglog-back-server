package com.campinglog.campinglogbackserver.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campinglog.campinglogbackserver.board.entity.Board;
import com.campinglog.campinglogbackserver.board.entity.BoardLike;
import com.campinglog.campinglogbackserver.board.repository.BoardRepository;
import com.campinglog.campinglogbackserver.member.dto.request.*;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberProfileImage;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.exception.*;
import com.campinglog.campinglogbackserver.member.repository.MemberRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Slf4j
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(OrderAnnotation.class)
@Transactional
class MemberServiceTest {

  @Autowired
  private MemberServiceImpl memberService;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  @Autowired
  private MemberRepository memberRepository;
    @Autowired
    private BoardRepository boardRepository;

  @Test
  @Order(1)
  public void addMember_ValidDate_Success() {
    // given
    RequestAddMember requestAddMember = RequestAddMember.builder()
        .email("testuser@example.com")
        .password("Password123!") // 테스트용 평문 비밀번호
        .name("홍길동")
        .nickname("길동이")
        .birthday(LocalDate.of(1995, 8, 15))
        .phoneNumber("010-1234-5678")
        .build();

    // when
    memberService.addMember(requestAddMember);
    Optional<Member> resultMember = memberRepository.findByEmail(requestAddMember.getEmail());

    // then
    assertThat(requestAddMember.getPassword()).isNotEqualTo(resultMember.get().getPassword());
    assertThat(passwordEncoder.matches(requestAddMember.getPassword(),
        resultMember.get().getPassword())).isTrue();

  }

  @Test
  @Order(2)
  public void addMember_duplicateEmail_ThrowsException() {
    // given
    RequestAddMember req1 = RequestAddMember.builder()
            .email("dupadd@example.com")
            .password("pw")
            .name("유저1")
            .nickname("닉1")
            .birthday(LocalDate.of(1990,1,1))
            .phoneNumber("010-1111-0000")
            .build();
    memberService.addMember(req1);

    RequestAddMember req2 = RequestAddMember.builder()
            .email("dupadd@example.com") // 같은 이메일
            .password("pw2")
            .name("유저2")
            .nickname("닉2")
            .birthday(LocalDate.of(1991,1,1))
            .phoneNumber("010-2222-0000")
            .build();

    // when & then
    assertThatThrownBy(() -> memberService.addMember(req2))
            .isInstanceOf(DuplicateEmailError.class);
  }

  @Test
  @Order(3)
  public void checkEmailAvailable_duplicateEmail_ThrowsException() {
    // given
    RequestAddMember req = RequestAddMember.builder()
            .email("dup@example.com")
            .password("pw")
            .name("tester")
            .nickname("dup")
            .birthday(LocalDate.of(2000, 1, 1))
            .phoneNumber("010-0000-0000")
            .build();
    memberService.addMember(req);

    // when & then
    assertThatThrownBy(() -> memberService.checkEmailAvailable("dup@example.com"))
            .isInstanceOf(DuplicateEmailError.class);
  }

  @Test
  @Order(4)
  public void checkNicknameAvailable_duplicateNickname_ThrowsException() {
    // given
    RequestAddMember req = RequestAddMember.builder()
            .email("nick@example.com")
            .password("pw")
            .name("tester")
            .nickname("닉중복")
            .birthday(LocalDate.of(2000, 1, 1))
            .phoneNumber("010-0000-1111")
            .build();
    memberService.addMember(req);

    // when & then
    assertThatThrownBy(() -> memberService.checkNicknameAvailable("닉중복"))
            .isInstanceOf(DuplicateNicknameError.class);
  }

  @Test
  @Order(5)
  public void verifyPassword_wrongPassword_ThrowsException() {
    // given
    RequestAddMember req = RequestAddMember.builder()
            .email("pwtest@example.com")
            .password("Password123!")
            .name("비번테스트")
            .nickname("pwnick")
            .birthday(LocalDate.of(1999, 9, 9))
            .phoneNumber("010-9999-9999")
            .build();
    memberService.addMember(req);

    RequestVerifyPassword wrong = new RequestVerifyPassword("WrongPassword!");

    // when & then
    assertThatThrownBy(() -> memberService.verifyPassword(req.getEmail(), wrong))
            .isInstanceOf(PasswordMismatchError.class);
  }

  @Test
  @Order(6)
  public void verifyPassword_nonExistingEmail_ThrowsException() {
    // given
    RequestVerifyPassword req = new RequestVerifyPassword("anyPw");

    // when & then
    assertThatThrownBy(() -> memberService.verifyPassword("noone@example.com", req))
            .isInstanceOf(RuntimeException.class); // → 실제 구현 시 MemberNotFoundError 등 구체화 가능
  }


  @Test
  @Order(7)
  public void setPassword_success() {
    // given
    String email = "changepw@example.com";
    String oldPw = "OldPassword1!";
    RequestAddMember req = RequestAddMember.builder()
            .email(email)
            .password(oldPw)
            .name("PwUser")
            .nickname("pwuser")
            .birthday(LocalDate.of(1995, 5, 5))
            .phoneNumber("010-5555-5555")
            .build();
    memberService.addMember(req);

    RequestChangePassword change = new RequestChangePassword(oldPw, "NewPassword1!");

    // when
    memberService.setPassword(email, change);

    // then
    Member updated = memberRepository.findByEmail(email).get();
    assertThat(passwordEncoder.matches("NewPassword1!", updated.getPassword())).isTrue();
  }

  @Test
  @Order(8)
  public void setPassword_wrongOldPassword_ThrowsException() {
    // given
    String email = "wrongpw@example.com";
    RequestAddMember req = RequestAddMember.builder()
            .email(email)
            .password("Correct1!")
            .name("유저")
            .nickname("nickw")
            .birthday(LocalDate.of(1990,1,1))
            .phoneNumber("010-1111-2222")
            .build();
    memberService.addMember(req);

    RequestChangePassword change = new RequestChangePassword("WrongOld!", "NewPw123!");

    // when & then
    assertThatThrownBy(() -> memberService.setPassword(email, change))
            .isInstanceOf(PasswordMismatchError.class);
  }

  @Test
  @Order(9)
  public void setMember_changeNickname_success() {
    // given
    String email = "update@example.com";
    RequestAddMember req = RequestAddMember.builder()
            .email(email)
            .password("pw")
            .name("OldName")
            .nickname("oldnick")
            .birthday(LocalDate.of(1990, 1, 1))
            .phoneNumber("010-1111-2222")
            .build();
    memberService.addMember(req);

    RequestUpdateMember updateReq = RequestUpdateMember.builder()
            .nickname("newnick")
            .build();

    // when
    memberService.setMember(email, updateReq);

    // then
    Member updated = memberRepository.findByEmail(email).get();
    assertThat(updated.getNickname()).isEqualTo("newnick");
  }

  @Test
  @Order(10)
  public void setMember_nonExisting_ThrowsException() {
    RequestUpdateMember req = RequestUpdateMember.builder()
            .nickname("nonick")
            .build();

    assertThatThrownBy(() -> memberService.setMember("ghost@example.com", req))
            .isInstanceOf(RuntimeException.class);
  }

  @Test
  @Order(11)
  public void addProfileImage_success() {
    // given
    String email = "img@example.com";
    RequestAddMember req = RequestAddMember.builder()
            .email(email)
            .password("pw")
            .name("이미지")
            .nickname("imgnick")
            .birthday(LocalDate.of(2000, 2, 2))
            .phoneNumber("010-2222-3333")
            .build();
    memberService.addMember(req);

    RequestSetProfileImage request = new RequestSetProfileImage("http://image.com/profile.png");

    // when
    memberService.addProfileImage(email, request);

    // then
    ResponseGetMemberProfileImage response = memberService.getProfileImage(email);
    assertThat(response.getProfileImage()).isEqualTo("http://image.com/profile.png");
  }

  @Test
  @Order(12)
  public void addProfileImage_nonExisting_ThrowsException() {
    RequestSetProfileImage req = new RequestSetProfileImage("http://image.com/x.png");

    assertThatThrownBy(() -> memberService.addProfileImage("ghost@example.com", req))
            .isInstanceOf(RuntimeException.class);
  }

  @Test
  @Order(13)
  public void getProfileImage_noImage_returnsNullOrDefault() {
    String email = "noimg@example.com";
    RequestAddMember req = RequestAddMember.builder()
            .email(email)
            .password("pw")
            .name("NoImg")
            .nickname("noimg")
            .birthday(LocalDate.of(1995, 1, 1))
            .phoneNumber("010-0000-0000")
            .build();

    memberService.addMember(req);

    // when & then
    assertThatThrownBy(() -> memberService.getProfileImage(email))
            .isInstanceOf(ProfileImageNotFoundError.class)
            .hasMessage("등록된 프로필 이미지가 없습니다."); // 또는 default 값 검증
  }

  @Test
  @Order(14)
  public void getMemberByEmail_nonExisting_ThrowsException() {
    // when & then
    assertThatThrownBy(() -> memberService.getMember("ghost@example.com"))
            .isInstanceOf(MemberNotFoundError.class);
  }

  @Test
  @Order(15)
  public void updateGradeWeekly_success() {
    // given
    String email = "grade@example.com";
    RequestAddMember req = RequestAddMember.builder()
            .email(email)
            .password("pw")
            .name("GradeUser")
            .nickname("grader")
            .birthday(LocalDate.of(1990, 1, 1))
            .phoneNumber("010-0000-1111")
            .build();
    memberService.addMember(req);
    Member member = memberRepository.findByEmail(email).get();

    // 게시글 생성 + 좋아요 수 직접 세팅
    Board board = Board.builder()
            .title("grade test")
            .content("c")
            .categoryName("free")
            .member(member)
            .likeCount(55)  // → 50 이상이므로 RED로 바뀌어야 함
            .build();
    boardRepository.save(board);

    // when
    int changed = memberService.updateGradeWeekly();

    // then
    Member updated = memberRepository.findByEmail(email).get();
    assertThat(changed).isEqualTo(1);
    assertThat(updated.getMemberGrade()).isEqualTo(Member.MemberGrade.RED);
  }

  @Test
  @Order(16)
  public void updateGradeWeekly_noBoards_returnsZero() {
    // given
    String email = "noboard@example.com";
    RequestAddMember req = RequestAddMember.builder()
            .email(email)
            .password("pw")
            .name("NoBoard")
            .nickname("noboard")
            .birthday(LocalDate.of(1995, 5, 5))
            .phoneNumber("010-5555-6666")
            .build();
    memberService.addMember(req);

    // when
    int changed = memberService.updateGradeWeekly();

    // then
    assertThat(changed).isEqualTo(0);
  }

  @Test
  @Order(17)
  public void updateRankWeekly_success() {
    // given
    RequestAddMember req1 = RequestAddMember.builder()
            .email("rank1@example.com")
            .password("pw")
            .name("랭커1")
            .nickname("r1")
            .birthday(LocalDate.of(1992, 2, 2))
            .phoneNumber("010-1111-2222")
            .build();
    memberService.addMember(req1);
    Member m1 = memberRepository.findByEmail(req1.getEmail()).get();

    RequestAddMember req2 = RequestAddMember.builder()
            .email("rank2@example.com")
            .password("pw")
            .name("랭커2")
            .nickname("r2")
            .birthday(LocalDate.of(1993, 3, 3))
            .phoneNumber("010-3333-4444")
            .build();
    memberService.addMember(req2);
    Member m2 = memberRepository.findByEmail(req2.getEmail()).get();

    // board 생성
    Board b1 = Board.builder().title("b1").content("c1").categoryName("free").member(m1).build();
    Board b2 = Board.builder().title("b2").content("c2").categoryName("free").member(m2).build();
    boardRepository.saveAll(List.of(b1, b2));

    // 좋아요 추가 (BoardLike는 Cascade에 의해 board에 추가 후 저장)
    BoardLike like1 = BoardLike.builder().board(b1).member(m2).build(); // m1 글에 좋아요 1개
    BoardLike like2 = BoardLike.builder().board(b1).member(m1).build(); // m1 글에 좋아요 2개
    BoardLike like3 = BoardLike.builder().board(b2).member(m1).build(); // m2 글에 좋아요 1개
    b1.getBoardLikes().addAll(List.of(like1, like2));
    b2.getBoardLikes().add(like3);
    boardRepository.saveAll(List.of(b1, b2));

    // when
    List<Map<String,Object>> ranks = memberService.updateRankWeekly(10);

    // then
    assertThat(ranks).hasSize(2);
    assertThat(ranks.get(0).get("email")).isEqualTo("rank1@example.com");
    assertThat(ranks.get(0).get("totalLikes")).isEqualTo(2L);
    assertThat(ranks.get(1).get("email")).isEqualTo("rank2@example.com");
    assertThat(ranks.get(1).get("totalLikes")).isEqualTo(1L);
  }

  @Test
  @Order(18)
  public void updateRankWeekly_noLikes_returnsEmptyList() {
    // given
    RequestAddMember req = RequestAddMember.builder()
            .email("norank@example.com")
            .password("pw")
            .name("랭커없음")
            .nickname("nrank")
            .birthday(LocalDate.of(1991, 1, 1))
            .phoneNumber("010-0000-7777")
            .build();
    memberService.addMember(req);

    // when
    List<Map<String,Object>> ranks = memberService.updateRankWeekly(5);

    // then
    assertThat(ranks).isEmpty();
  }

  @Test
  @Order(19)
  public void deleteMember_success() {
    // given
    String email = "del@example.com";
    RequestAddMember req = RequestAddMember.builder()
            .email(email)
            .password("pw")
            .name("삭제유저")
            .nickname("delnick")
            .birthday(LocalDate.of(1991, 1, 1))
            .phoneNumber("010-1234-9999")
            .build();
    memberService.addMember(req);

    // when
    memberService.deleteMember(email);

    // then
    Optional<Member> deleted = memberRepository.findByEmail(email);
    assertThat(deleted).isEmpty();
  }

  @Test
  @Order(20)
  public void deleteMember_nonExisting_ThrowsException() {
    // when & then
    assertThatThrownBy(() -> memberService.deleteMember("ghost@example.com"))
            .isInstanceOf(RuntimeException.class); // 실제로 MemberNotFoundError 로 구체화 필요
  }
}