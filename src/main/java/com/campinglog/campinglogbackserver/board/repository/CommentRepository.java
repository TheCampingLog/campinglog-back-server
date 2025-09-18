package com.campinglog.campinglogbackserver.board.repository;

import com.campinglog.campinglogbackserver.board.entity.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByCommentId(String commentId);

    @EntityGraph(attributePaths = {"member"})
    Page<Comment> findByBoard_IdOrderByCreatedAtDescIdDesc(Long boardIdPk, Pageable pageable);

}