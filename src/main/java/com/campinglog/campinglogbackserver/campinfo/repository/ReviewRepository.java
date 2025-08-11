package com.campinglog.campinglogbackserver.campinfo.repository;

import com.campinglog.campinglogbackserver.campinfo.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  List<Review> findByMapXAndMapY(String mapX, String mapY);

}
