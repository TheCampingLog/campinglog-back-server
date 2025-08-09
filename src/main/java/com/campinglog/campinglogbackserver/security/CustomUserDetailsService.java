package com.campinglog.campinglogbackserver.security;

import com.campinglog.campinglogbackserver.account.entity.Member;
import com.campinglog.campinglogbackserver.account.repository.MemberRespository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final MemberRespository memberRespository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

    Optional<Member> user = memberRespository.findByEmail(email);

    if (user.isEmpty()) {
      throw new UsernameNotFoundException("사용자를 찾을 수 가 없습니다");
    }

    return new CustomUserDetails(user.get());
  }
}
