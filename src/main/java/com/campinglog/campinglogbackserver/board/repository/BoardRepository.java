package com.campinglog.campinglogbackserver.board.repository;

import com.campinglog.campinglogbackserver.board.entity.Board;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.campinglog.campinglogbackserver.common.dto.MemberLikeSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    @EntityGraph(attributePaths = "member")
    Optional<Board> findByBoardId(String boardId);

    @EntityGraph(attributePaths = "member")
    List<Board> findByCreatedAtAfterOrderByLikeCountDescViewCountDescCreatedAtDesc(
        LocalDateTime from, Pageable pageable);

    @EntityGraph(attributePaths = "member")
    List<Board> findByTitleContainingOrderByCreatedAtDesc(String title, Pageable pageable);

    @EntityGraph(attributePaths = "member")
    List<Board> findByCategoryNameOrderByCreatedAtDesc(String categoryName, Pageable pageable);

    @EntityGraph(attributePaths = "member")
    Page<Board> findByEmail(String email, Pageable pageable);

    // 회원별 좋아요 합계 (글 없는 회원도 포함하려면 LEFT JOIN으로 Member 시작)
    @Query("""
     SELECT new com.campinglog.campinglogbackserver.common.dto.MemberLikeSummary(
       m.email,
       COALESCE(SUM(b.likeCount), 0)
     )
     FROM Member m
     LEFT JOIN m.boards b
     GROUP BY m.email
  """)
    List<MemberLikeSummary> sumLikesGroupByMember();

    // 특정 이메일의 합계가 필요하면 (선택)
    @Query("""
     SELECT COALESCE(SUM(b.likeCount), 0)
     FROM Board b
     WHERE b.member.email = :email
  """)
    long sumLikeCountByEmail(String email);

    Page<Board> findByMemberEmail(String email, PageRequest pageable);
}