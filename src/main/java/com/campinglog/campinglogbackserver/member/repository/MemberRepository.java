package com.campinglog.campinglogbackserver.member.repository;

import com.campinglog.campinglogbackserver.member.entity.Member;

import java.time.LocalDateTime;
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


  /**
   * [start, end) 구간 동안 발생한 좋아요를 기준으로
   * 회원별 '받은 좋아요 수' 합산.
   */
  @Query("""
      SELECT m.email        AS email,
             m.nickname     AS nickname,
             m.profileImage AS profileImage,
             COUNT(l)       AS totalLikes
      FROM BoardLike l
        JOIN l.board b
        JOIN b.member m
      WHERE l.createdAt >= :start AND l.createdAt < :end
      GROUP BY m.email, m.nickname, m.profileImage
      ORDER BY COUNT(l) DESC
    """)
  List<WeeklyLikeAggRow> findTopMembersByLikeCreatedAt(
          @Param("start") LocalDateTime start,
          @Param("end") LocalDateTime end
  );

  public interface WeeklyLikeAggRow {
    String getEmail();
    String getNickname();
    String getProfileImage();
    Long getTotalLikes();
  }
}
