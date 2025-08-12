package com.campinglog.campinglogbackserver.member.repository;

import com.campinglog.campinglogbackserver.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRespository extends JpaRepository<Member, String> {

  Optional<Member> findByEmail(String email);
  boolean existsByEmail(String email);
  boolean existsByNickname(String nickname);
  boolean existsByNicknameAndEmailNot(String nickname, String email);
  boolean existsByPhoneNumberAndEmailNot(String phoneNumber, String email);

  @Query("select coalesce(sum(b.likeCount), 0) from Board b where b.email = :email")
  long sumLikeCountByEmail(@Param("email") String email);
}
