package com.campinglog.campinglogbackserver.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campinglog.campinglogbackserver.board.entity.Board;
import com.campinglog.campinglogbackserver.board.entity.BoardLike;
import com.campinglog.campinglogbackserver.board.repository.BoardRepository;
import com.campinglog.campinglogbackserver.member.entity.Member;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.orm.jpa.JpaSystemException;

@DataJpaTest
@Slf4j
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(OrderAnnotation.class)
class MemberRepositoryTests {

  @Autowired
  public MemberRepository memberRepository;
    @Autowired
    private BoardRepository boardRepository;

  @Test
  @Order(1)
  public void findByEmail_validData_Success() {

    // given
    String email = "test@example.com";

    // when
    Optional<Member> resultMember = memberRepository.findByEmail(email);

    // then
    assertThat(resultMember).isPresent();
  }

  @Test
  @Order(2)
  public void findByEmail_NonExistingEmail_ReturnEmpty() {

    // given
    String email = "test2@example.com";

    // when
    Optional<Member> resultMember = memberRepository.findByEmail(email);

    // then
    assertThat(resultMember).isEmpty();

  }

  @Test
  @Order(3)
  public void addMember() {
    // given
    Member member = Member.builder()
        .email("testuser@example.com")
        .password("Password123!")  // 실제 테스트 시 암호화 처리 고려
        .name("홍길동")
        .nickname("길동이")
        .birthday(LocalDate.of(1995, 8, 15))
        .phoneNumber("010-1234-5678")
        .build();

    // when
    Member resultMember = memberRepository.save(member);

    // then
    assertThat(resultMember).isNotNull();
    assertThat(resultMember.getEmail()).isEqualTo("testuser@example.com");

  }

  @Test
  @Order(4)
  public void addMember_NullEmail_JpaSystemException() {
    // given
    Member member = Member.builder()
        .password("Password123!")  // 실제 테스트 시 암호화 처리 고려
        .name("홍길동")
        .nickname("길동이")
        .birthday(LocalDate.of(1995, 8, 15))
        .phoneNumber("010-1234-5678")
        .build();

    // when & then
    assertThatThrownBy(() -> {
      memberRepository.save(member);
    }).isInstanceOf(JpaSystemException.class);

  }
  @Test
  @Order(5)
  public void existsByEmail_existingEmail_ReturnTrue() {
    // given
    String email = "test@example.com";

    // when
    boolean exists = memberRepository.existsByEmail(email);

    // then
    assertThat(exists).isTrue();
  }

  @Test
  @Order(6)
  public void existsByEmail_nonExistingEmail_ReturnFalse() {
    // given
    String email = "nonexistent@example.com";

    // when
    boolean exists = memberRepository.existsByEmail(email);

    // then
    assertThat(exists).isFalse();
  }

  @Test
  @Order(7)
  public void existsByNickname_existingNickname_ReturnTrue() {
    // given
    String nickname = "길동이";

    // when
    boolean exists = memberRepository.existsByNickname(nickname);

    // then
    assertThat(exists).isTrue();
  }

  @Test
  @Order(8)
  public void existsByNickname_nonExistingNickname_ReturnFalse() {
    // given
    String nickname = "없는닉네임";

    // when
    boolean exists = memberRepository.existsByNickname(nickname);

    // then
    assertThat(exists).isFalse();
  }

  @Test
  @Order(9)
  public void existsByNicknameAndEmailNot_sameNicknameDifferentEmail_ReturnTrue() {
    // given
    Member another = Member.builder()
            .email("another@example.com")
            .password("Password456!")
            .name("김철수")
            .nickname("길동이") // 중복 닉네임
            .birthday(LocalDate.of(1993, 5, 10))
            .phoneNumber("010-2222-3333")
            .build();
    memberRepository.save(another);

    // when
    boolean exists = memberRepository.existsByNicknameAndEmailNot("길동이", "another@example.com");

    // then
    assertThat(exists).isTrue();
  }

  @Test
  @Order(10)
  public void existsByNicknameAndEmailNot_sameNicknameSameEmail_ReturnFalse() {
    // given
    String email = "testuser@example.com";

    // when
    boolean exists = memberRepository.existsByNicknameAndEmailNot("길동이", email);

    // then
    assertThat(exists).isFalse();
  }

  @Test
  @Order(11)
  public void existsByPhoneNumberAndEmailNot_samePhoneDifferentEmail_ReturnTrue() {
    // given
    Member another = Member.builder()
            .email("duplicatephone@example.com")
            .password("Password789!")
            .name("이영희")
            .nickname("영희")
            .birthday(LocalDate.of(1992, 3, 20))
            .phoneNumber("010-1234-5678") // 동일 전화번호
            .build();
    memberRepository.save(another);

    // when
    boolean exists = memberRepository.existsByPhoneNumberAndEmailNot("010-1234-5678", "duplicatephone@example.com");

    // then
    assertThat(exists).isTrue();
  }

  @Test
  @Order(12)
  public void existsByPhoneNumberAndEmailNot_samePhoneSameEmail_ReturnFalse() {
    // given
    String email = "testuser@example.com";

    // when
    boolean exists = memberRepository.existsByPhoneNumberAndEmailNot("010-1234-5678", email);

    // then
    assertThat(exists).isFalse();
  }

  @Test
  @Order(13)
  public void findTopMembersByLikeCreatedAt_validRange_ReturnsAggregatedLikes() {
    // given
    LocalDateTime start = LocalDateTime.now().minusDays(7);
    LocalDateTime end = LocalDateTime.now().plusDays(1);

    // 테스트용 회원1
    Member member1 = Member.builder()
            .email("like1@example.com")
            .password("pw")
            .name("사용자1")
            .nickname("닉네임1")
            .birthday(LocalDate.of(1990, 1, 1))
            .phoneNumber("010-1111-1111")
            .build();
    memberRepository.save(member1);

    // 테스트용 회원2
    Member member2 = Member.builder()
            .email("like2@example.com")
            .password("pw")
            .name("사용자2")
            .nickname("닉네임2")
            .birthday(LocalDate.of(1992, 2, 2))
            .phoneNumber("010-2222-2222")
            .build();
    memberRepository.save(member2);

    // 여기서 Board, BoardLike 엔티티를 저장해서 좋아요 데이터 심어줘야 함
    // (예: member1 게시글에 좋아요 2개, member2 게시글에 좋아요 1개)
    // repository.save(...) or testEntityManager.persist(...) 사용

    // when
    List<MemberRepository.WeeklyLikeAggRow> results =
            memberRepository.findTopMembersByLikeCreatedAt(start, end);

    // then
    assertThat(results).isNotEmpty();
    assertThat(results.get(0).getEmail()).isEqualTo("like1@example.com");
    assertThat(results.get(0).getTotalLikes()).isGreaterThanOrEqualTo(2L);

    assertThat(results.get(1).getEmail()).isEqualTo("like2@example.com");
    assertThat(results.get(1).getTotalLikes()).isGreaterThanOrEqualTo(1L);
  }

  @Test
  @Order(14)
  public void findTopMembersByLikeCreatedAt_emptyRange_ReturnsEmpty() {
    // given
    LocalDateTime start = LocalDateTime.now().plusDays(1);
    LocalDateTime end = LocalDateTime.now().plusDays(2);

    // when
    List<MemberRepository.WeeklyLikeAggRow> results =
            memberRepository.findTopMembersByLikeCreatedAt(start, end);

    // then
    assertThat(results).isEmpty();
  }

  @Test
  @Order(20)
  void findTopMembersByLikeCreatedAt_validRange_success() {
    // given
    Member member1 = Member.builder()
            .email("like1@example.com")
            .password("pw")
            .name("사용자1")
            .nickname("닉1")
            .birthday(LocalDate.of(1990, 1, 1))
            .phoneNumber("010-1111-1111")
            .build();
    memberRepository.save(member1);

    Member member2 = Member.builder()
            .email("like2@example.com")
            .password("pw")
            .name("사용자2")
            .nickname("닉2")
            .birthday(LocalDate.of(1992, 2, 2))
            .phoneNumber("010-2222-2222")
            .build();
    memberRepository.save(member2);

    // member1이 작성한 게시글
    Board board1 = Board.builder()
            .title("board1")
            .content("content1")
            .categoryName("free")
            .member(member1)
            .build();
    boardRepository.save(board1);

    // member2가 작성한 게시글
    Board board2 = Board.builder()
            .title("board2")
            .content("content2")
            .categoryName("free")
            .member(member2)
            .build();
    boardRepository.save(board2);

    // 좋아요 데이터: board1(2개), board2(1개)
    BoardLike like1 = BoardLike.builder().board(board1).member(member2).build();
    BoardLike like2 = BoardLike.builder().board(board1).member(member2).build();
    BoardLike like3 = BoardLike.builder().board(board2).member(member1).build();
    board1.getBoardLikes().add(like1);
    board1.getBoardLikes().add(like2);
    board2.getBoardLikes().add(like3);

    boardRepository.save(board1);
    boardRepository.save(board2);

    LocalDateTime start = LocalDateTime.now().minusDays(1);
    LocalDateTime end = LocalDateTime.now().plusDays(1);

    // when
    List<MemberRepository.WeeklyLikeAggRow> results =
            memberRepository.findTopMembersByLikeCreatedAt(start, end);

    // then
    assertThat(results).hasSize(2);
    assertThat(results.get(0).getEmail()).isEqualTo("like1@example.com");
    assertThat(results.get(0).getTotalLikes()).isEqualTo(2L);
    assertThat(results.get(1).getEmail()).isEqualTo("like2@example.com");
    assertThat(results.get(1).getTotalLikes()).isEqualTo(1L);
  }

  @Test
  @Order(21)
  void findTopMembersByLikeCreatedAt_emptyRange_returnsEmpty() {
    // given
    LocalDateTime start = LocalDateTime.now().plusDays(1);
    LocalDateTime end = LocalDateTime.now().plusDays(2);

    // when
    List<MemberRepository.WeeklyLikeAggRow> results =
            memberRepository.findTopMembersByLikeCreatedAt(start, end);

    // then
    assertThat(results).isEmpty();
  }
}