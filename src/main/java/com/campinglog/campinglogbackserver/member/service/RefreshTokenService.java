package com.campinglog.campinglogbackserver.member.service;

import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetToken;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.entity.RefreshToken;
import com.campinglog.campinglogbackserver.member.exception.InvalidRefreshTokenError;
import com.campinglog.campinglogbackserver.member.exception.RefreshTokenNotFoundError;
import com.campinglog.campinglogbackserver.member.repository.MemberRepository;
import com.campinglog.campinglogbackserver.member.repository.RefreshTokenRepository;
import com.campinglog.campinglogbackserver.security.JwtTokenProvider;
import com.campinglog.campinglogbackserver.security.RefreshTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final MemberRepository memberRepository;
  private final RefreshTokenProvider refreshTokenProvider;
  private final JwtTokenProvider jwtTokenProvider;

  public ResponseGetToken RotateRefreshToken(HttpServletRequest request) {

    String refreshToken = null;
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("s_rt".equals(cookie.getName())) {
          refreshToken = cookie.getValue();
          break;
        }
      }
    }

    if (refreshToken == null) {
      throw new InvalidRefreshTokenError("리프레시 토큰이 null입니다");
    }

    Optional<RefreshToken> requestRefreshToken;

    try {
      String jti = refreshTokenProvider.parseJwtToken(refreshToken).getId();
      requestRefreshToken = refreshTokenRepository.findByJti(jti);

    } catch (io.jsonwebtoken.ExpiredJwtException e) {
      throw new InvalidRefreshTokenError("리프레시 토큰이 만료되었습니다");
    } catch (io.jsonwebtoken.SignatureException e) {
      throw new InvalidRefreshTokenError("리프레시 토큰의 서명이 유효하지 않습니다");
    } catch (io.jsonwebtoken.MalformedJwtException e) {
      throw new InvalidRefreshTokenError("리프레시 토큰 형식이 올바르지 않습니다");
    } catch (io.jsonwebtoken.JwtException e) {
      throw new InvalidRefreshTokenError("리프레시 토큰이 유효하지 않습니다: " + e.getMessage());
    } catch (Exception e) {
      throw new RuntimeException("리프레시 토큰 처리 중 데이터베이스 오류가 발생했습니다: " + e.getMessage(), e);
    }

    if (refreshToken.isEmpty()) {
      throw new RefreshTokenNotFoundError("리프레시 토큰을 찾을 수 없습니다");
    }

    RefreshToken oldRefreshToken = requestRefreshToken.get();

    if (!oldRefreshToken.isValid()) {
      throw new InvalidRefreshTokenError("유효하지 않는 리프레시 토큰입니다");
    }

    //Access 토큰 재발급 후 토큰 추가
    String email = oldRefreshToken.getMember().getEmail();

    Member member = memberRepository.findByEmail(email)
        .orElseThrow(
            () -> new UsernameNotFoundException("Member not found with email: " + email));

    String newJwtToken = jwtTokenProvider.generateToken(member);

    //기존 리프레시 토큰을 used로 바꿈
    oldRefreshToken.setUsed(1);
    refreshTokenRepository.save(oldRefreshToken);
    //새로운 리프레시 토큰을 쿠키에 추가
    String newRefreshToken = refreshTokenProvider.generateToken();

    saveRefreshToken(newRefreshToken, member);

    return ResponseGetToken.builder().jwtToken(newJwtToken).refreshToken(newRefreshToken).build();

  }


  public void saveRefreshToken(String refreshToken, Member member) {
    saveRefreshToken(createRefreshToken(refreshToken, member));
  }

  public void saveRefreshToken(RefreshToken refreshToken) {
    System.out.println("리프레시 토큰 저장");
    refreshTokenRepository.save(refreshToken);
  }

  public RefreshToken createRefreshToken(String refreshToken, Member member) {
    return RefreshToken.builder()
        .jti(refreshTokenProvider.parseJwtToken(refreshToken).get("jti", String.class))
        .member(member)
        .used(0)
        .expiresAt(refreshTokenProvider.parseJwtToken(refreshToken).getExpiration())
        .build();
  }
}
