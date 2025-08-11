package com.campinglog.campinglogbackserver.board.repository;

import com.campinglog.campinglogbackserver.board.entity.Comment;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBoardIdOrderByCreatedAtDesc(String boardId, Pageable pageable);
}