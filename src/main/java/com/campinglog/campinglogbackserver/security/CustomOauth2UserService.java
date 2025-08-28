package com.campinglog.campinglogbackserver.security;

import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.repository.MemberRepository;
import com.campinglog.campinglogbackserver.member.service.MemberService;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

  private final MemberRepository memberRespository;
  private final MemberService memberService;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

    log.info("Access Token: {}", userRequest.getAccessToken().getTokenValue());

    OAuth2User oAuth2User = super.loadUser(userRequest);

    log.info("검증 시작");

    String registeredId = userRequest.getClientRegistration().getRegistrationId();
    String email = null;
    String nickname = null;
    String fakePassword = bCryptPasswordEncoder.encode(UUID.randomUUID().toString());

    if ("kakao".equals(registeredId)) {
      Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttributes()
          .get("kakao_account");
      if (kakaoAccount == null) {
        throw new OAuth2AuthenticationException("카카오 계정 정보를 가져올 수 없습니다.");
      }

      email = (String) kakaoAccount.get("email");
      Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
      nickname = profile != null ? (String) profile.get("nickname") : null;
    }

    if (email == null) {
      throw new OAuth2AuthenticationException("이메일 정보를 가져올 수 없습니다.");
    }

    if (nickname == null || nickname.isEmpty()) {
      nickname = "user" + UUID.randomUUID().toString().substring(0, 5);
    }

    if (memberRespository.existsByNickname(nickname)) {
      nickname += UUID.randomUUID().toString().substring(0, 5);
    }

    Optional<Member> optionalMember = memberRespository.findByEmail(email);
    Member member;

    if (optionalMember.isEmpty()) {
      member = Member.builder()
          .email(email)
          .name(nickname)
          .password(fakePassword)
          .nickname(nickname)
          .birthday(LocalDate.of(1999, 3, 20))
          .phoneNumber("010-1234-1234")
          .oauth(true)
          .build();
      memberRespository.save(member);
    } else {
      member = optionalMember.get();
      if (!member.getOauth()) {
        member.setOauth(true);
        memberRespository.save(member);
      }
    }

    return new CustomUserDetails(member, oAuth2User.getAttributes());
  }

}
