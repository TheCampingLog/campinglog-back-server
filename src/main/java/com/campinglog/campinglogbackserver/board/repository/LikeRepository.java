package com.campinglog.campinglogbackserver.board.repository;

import com.campinglog.campinglogbackserver.board.entity.Like;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByBoardIdAndEmail(String boardId, String email);

    Optional<Like> findByBoardIdAndEmail(String boardId, String email);
}
