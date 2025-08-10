package com.campinglog.campinglogbackserver.campinfo.repository;

import com.campinglog.campinglogbackserver.campinfo.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ReviewRepository extends JpaRepository<Review, Long> {


}
