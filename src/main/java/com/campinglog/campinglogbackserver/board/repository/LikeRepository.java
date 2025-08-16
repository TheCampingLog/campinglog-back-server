package com.campinglog.campinglogbackserver.board.repository;

import com.campinglog.campinglogbackserver.board.entity.BoardLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<BoardLike, Long> {

    boolean existsByBoardBoardIdAndMemberEmail(String boardId, String email);

    @EntityGraph(attributePaths = {"board", "member"})
    Optional<BoardLike> findByBoardBoardIdAndMemberEmail(String boardId, String email);

}