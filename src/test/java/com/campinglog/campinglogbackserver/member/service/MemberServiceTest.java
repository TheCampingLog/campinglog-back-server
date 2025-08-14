package com.campinglog.campinglogbackserver.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.campinglog.campinglogbackserver.member.dto.request.RequestAddMember;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.repository.MemberRepository;
import java.time.LocalDate;
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

  @Test
  @Order(1)
  public void addCafe_ValidDate_Success() {
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


}