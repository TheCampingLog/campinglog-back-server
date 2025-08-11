package com.campinglog.campinglogbackserver.campinfo.repository;

import com.campinglog.campinglogbackserver.campinfo.entity.Review;
import com.campinglog.campinglogbackserver.campinfo.entity.ReviewOfBoard;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewOfBoardRepository extends JpaRepository<ReviewOfBoard, Long> {
  ReviewOfBoard findByMapXAndMapY(String mapX, String mapY);
  boolean existsByMapXAndMapY(String mapX, String mapY);
  interface BoardRankView {
    Long getId();
    String getMapX();
    String getMapY();
    Double getReviewAverage();
  }
  List<BoardRankView> findAllByReviewAverageIsNotNull(Pageable pageable);
}
