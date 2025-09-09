package com.campinglog.campinglogbackserver.member.service;

import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.entity.RefreshToken;
import com.campinglog.campinglogbackserver.member.repository.MemberRepository;
import com.campinglog.campinglogbackserver.member.repository.RefreshTokenRepository;
import com.campinglog.campinglogbackserver.security.RefreshTokenProvider;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Slf4j
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(OrderAnnotation.class)
@Transactional
class RefreshTokenServiceTest {

  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private RefreshTokenRepository refreshTokenRepository;
  @Autowired
  private RefreshTokenProvider refreshTokenProvider;
  @Autowired
  private RefreshTokenService refreshTokenService;

  @Test
  void saveRefreshToken() {

    String refreshToken = refreshTokenProvider.generateToken();
    Member testMember = memberRepository.findByEmail("test@example.com").get();
    String testJti = refreshTokenProvider.parseJwtToken(refreshToken).get("jti", String.class);

    refreshTokenService.saveRefreshToken(refreshToken, testMember);

    Optional<RefreshToken> resultRefreshToken = refreshTokenRepository.findByJti(testJti);
    Assertions.assertThat(resultRefreshToken).isPresent();


  }

}