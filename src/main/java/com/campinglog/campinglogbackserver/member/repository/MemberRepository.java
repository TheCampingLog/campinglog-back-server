package com.campinglog.campinglogbackserver.member.repository;

import com.campinglog.campinglogbackserver.member.entity.Member;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, String> {

  Optional<Member> findByEmail(String email);
  boolean existsByEmail(String email);
  boolean existsByNickname(String nickname);
  boolean existsByNicknameAndEmailNot(String nickname, String email);
  boolean existsByPhoneNumberAndEmailNot(String phoneNumber, String email);
}
