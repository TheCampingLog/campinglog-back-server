package com.campinglog.campinglogbackserver.member.repository;

import com.campinglog.campinglogbackserver.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRespository extends JpaRepository<Member, String> {

  Optional<Member> findByEmail(String email);
  boolean existsByEmail(String email);
  boolean existsByNickname(String nickname);
  boolean existsByPhoneNumber(String phoneNumber);
  boolean existsByNicknameAndEmailNot(String nickname, String email);
  boolean existsByPhoneNumberAndEmailNot(String phoneNumber, String email);
}
