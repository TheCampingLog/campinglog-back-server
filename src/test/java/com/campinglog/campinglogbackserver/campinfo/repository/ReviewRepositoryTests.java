package com.campinglog.campinglogbackserver.campinfo.repository;

import com.campinglog.campinglogbackserver.campinfo.entity.Review;
import com.campinglog.campinglogbackserver.campinfo.entity.ReviewOfBoard;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.entity.Member.MemberGrade;
import com.campinglog.campinglogbackserver.member.entity.Member.Role;
import com.campinglog.campinglogbackserver.member.repository.MemberRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ReviewRepositoryTests {
  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  TestEntityManager testEntityManager;

  @Test
  @Sql(statements = {
      "INSERT INTO members (email, name, nickname, password, phone_number, birthday, join_date, member_grade, role)" +
          "VALUES ('test13@test.com', 'User', 'u1nickname', 'pw', '010-0000-0000', '1999-01-01', '2025-08-01', 'GREEN', 'USER')"
  })
  public void findByMapXAndMapY_ValidData_Success() {
    // given
    String email = "test13@test.com";

    Member memberRef = Member.builder().email(email).build();
    reviewRepository.save(Review.builder()
        .member(memberRef)
        .mapX("127.2636514")
        .mapY("37.0323408")
        .reviewScore(4.5)
        .reviewContent("굿")
        .build()
    );

    int page = 0;
    int size = 1;
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postAt"));

    // when
    Page<Review> reviews = reviewRepository.findByMapXAndMapY("127.2636514", "37.0323408", pageable);

    // then
    assertThat(reviews.hasContent()).isTrue();
  }

  @Test
  public void findByMapXAndMapY_InvalidData_ReturnEmpty() {
    // given
    String mapX = "127.2636514";
    String mapY = "37.0323408";

    int page = 0;
    int size = 1;
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postAt"));

    // when
    Page<Review> reviews = reviewRepository.findByMapXAndMapY(mapX, mapY, pageable);

    // then
    assertThat(reviews.hasContent()).isFalse();
    assertThat(reviews.getTotalElements()).isZero();
    assertThat(reviews.getContent()).isEmpty();
  }

  @Test
  @Sql(statements = {
      "INSERT INTO members (email, name, nickname, password, phone_number, birthday, join_date, member_grade, role)" +
          "VALUES ('test13@test.com', 'User', 'u1nickname', 'pw', '010-0000-0000', '1999-01-01', '2025-08-01', 'GREEN', 'USER')"
  })
  public void findByMember_Email_VaildData_success() {
    // given
    String email = "test13@test.com";
    int page = 0;
    int size = 1;
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postAt"));

    Member memberRef = Member.builder().email(email).build();

    Review firstReview = Review.builder()
        .member(memberRef)
        .mapX("127.2636514")
        .mapY("37.0323408")
        .reviewScore(4.5)
        .reviewContent("굿")
        .build();
    Review secondReview = Review.builder()
        .member(memberRef)
        .mapX("128.329336397232")
        .mapY("34.8867971280615")
        .reviewScore(4.5)
        .reviewContent("굿")
        .build();

    reviewRepository.saveAll(List.of(firstReview, secondReview));

    // when
    Page<Review> reviewPage = reviewRepository.findByMember_Email(email, pageable);

    // then
    assertThat(reviewPage.getTotalElements()).isEqualTo(2);
    assertThat(reviewPage.getContent()).extracting(review -> review.getMember().getEmail())
        .containsOnly(email);
  }

  @Test
  public void findByEmail_NonExistingEmail_ReturnEmpty() {
    // given
    String email = "test15@test.com";
    int page = 0;
    int size = 1;
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postAt"));

    // when
    Page<Review> reviewPage = reviewRepository.findByMember_Email(email, pageable);

    // then
    assertThat(reviewPage.getTotalElements()).isZero();
    assertThat(reviewPage.getContent()).isEmpty();
  }




}
