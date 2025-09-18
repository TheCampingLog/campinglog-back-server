package com.campinglog.campinglogbackserver.security;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest
class RefreshTokenProviderTest {

  @Autowired
  private RefreshTokenProvider refreshTokenProvider;


  @Test
  void generateRefreshToken_Success() {
    String refreshToken = refreshTokenProvider.generateToken();
    assertThat(refreshToken).isNotNull();

    String[] parts = refreshToken.split("\\.");
    assertThat(parts.length).isEqualTo(3);
  }

}