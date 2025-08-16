package com.campinglog.campinglogbackserver.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campinglog.campinglogbackserver.member.entity.Member;
import java.time.LocalDate;
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

}