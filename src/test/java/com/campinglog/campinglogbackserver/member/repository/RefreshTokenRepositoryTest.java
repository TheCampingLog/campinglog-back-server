package com.campinglog.campinglogbackserver.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.entity.RefreshToken;
import java.util.Date;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@Slf4j
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(OrderAnnotation.class)
class RefreshTokenRepositoryTest {

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private MemberRepository memberRepository;

  @BeforeEach
  public void setUp() {
    Optional<Member> existMember = memberRepository.findByEmail("test@example.com");

    RefreshToken refreshToken = RefreshToken.builder()
        .jti("unique-jti-111")
        .member(existMember.get())
        .used(0)
        .expiresAt(new Date(System.currentTimeMillis() + 86400000))
        .build();

    refreshTokenRepository.save(refreshToken);

  }

  @Test
  @Order(1)
  public void addRefreshToken_Success() {
    Optional<Member> existMember = memberRepository.findByEmail("test@example.com");

    RefreshToken refreshToken = RefreshToken.builder()
        .jti("unique-jti-123")
        .member(existMember.get())
        .used(0)
        .expiresAt(new Date(System.currentTimeMillis() + 86400000))
        .build();

    RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshToken);

    assertThat(savedRefreshToken).isNotNull();
  }

  @Test
  @Order(2)
  public void findByJti_validData_Success() {
    Optional<RefreshToken> existRefreshToken = refreshTokenRepository.findByJti(
        "unique-jti-111");

    assertThat(existRefreshToken.get()).isNotNull();
  }

  @Test
  @Order(3)
  public void findByJti_NotExistingJti_Null() {
    Optional<RefreshToken> nonExistToken = refreshTokenRepository.findByJti(
        "non-existing-jti-999");

    assertThat(nonExistToken).isEmpty();
  }


}