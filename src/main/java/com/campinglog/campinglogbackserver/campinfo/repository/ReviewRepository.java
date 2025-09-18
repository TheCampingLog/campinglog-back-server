package com.campinglog.campinglogbackserver.campinfo.repository;

import com.campinglog.campinglogbackserver.campinfo.entity.Review;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  @EntityGraph(attributePaths = "member")
  Page<Review> findByMapXAndMapY(String mapX, String mapY, Pageable pageable);
  Page<Review> findByMember_Email(String email, Pageable pageable);
}
