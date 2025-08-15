package com.campinglog.campinglogbackserver.board.repository;

import com.campinglog.campinglogbackserver.board.entity.Board;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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
}