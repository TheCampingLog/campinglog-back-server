package com.campinglog.campinglogbackserver.campinfo.repository;

import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestAddReview;
import com.campinglog.campinglogbackserver.campinfo.entity.ReviewOfBoard;
import com.campinglog.campinglogbackserver.campinfo.repository.ReviewOfBoardRepository.BoardRankView;
import com.campinglog.campinglogbackserver.campinfo.service.CampInfoServiceImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ReviewOfBoardRepositoryTests {
  @Autowired
  private ReviewOfBoardRepository reviewOfBoardRepository;

  @Test
  public void findByMapXAndMapY_ValidLocation_Success() {
    // given
    String mapX = "127.2636514";
    String mapY = "37.0323408";
    reviewOfBoardRepository.save(
        ReviewOfBoard.builder()
            .mapY("37.0323408")
            .mapX("127.2636514")
            .reviewAverage(4.5)
            .reviewCount(5)
            .build()
    );

    // when
    ReviewOfBoard reviewOfBoard = reviewOfBoardRepository.findByMapXAndMapY(mapX, mapY);

    // then
    assertThat(reviewOfBoard)
        .isNotNull()
        .extracting(ReviewOfBoard::getMapX, ReviewOfBoard::getMapY)
        .containsExactly(mapX, mapY);
  }

  @Test
  public void findByMapXAndMapY_NotExistingCamp_ReturnEmpty() {
    // given
    reviewOfBoardRepository.save(
        ReviewOfBoard.builder()
            .mapY("37.0323408")
            .mapX("127.2636514")
            .reviewAverage(4.5)
            .reviewCount(5)
            .build()
    );
    String mapX = "999";
    String mapY = "999";

    // when
    ReviewOfBoard reviewOfBoard = reviewOfBoardRepository.findByMapXAndMapY(mapX, mapY);

    // then
    assertThat(reviewOfBoard).isNull();
  }

  @Test
  public void deleteByMapXAndMapY_ValidLocation_Success() {
    // given
    String mapX = "127.2636514";
    String mapY = "37.0323408";
    reviewOfBoardRepository.save(
        ReviewOfBoard.builder()
            .mapY(mapY)
            .mapX(mapX)
            .reviewAverage(4.5)
            .reviewCount(5)
            .build()
    );

    // when
    reviewOfBoardRepository.deleteByMapXAndMapY(mapX, mapY);

    // then
    assertThat(reviewOfBoardRepository.findByMapXAndMapY(mapX, mapY)).isNull();

  }

  @Test
  public void deleteByMapXAndMapY_NonExistLocation_ReturnNull() {
    // given
    String mapX = "127.2636514";
    String mapY = "37.0323408";

    // when
    long affected = reviewOfBoardRepository.deleteByMapXAndMapY(mapX, mapY);

    // then
    assertThat(affected).isZero();
  }

  @Test
  public void existsByMapXAndMapY_ValidLocation_Success() {
    // given
    String mapX = "127.2636514";
    String mapY = "37.0323408";
    reviewOfBoardRepository.save(
        ReviewOfBoard.builder()
            .mapY(mapY)
            .mapX(mapX)
            .reviewAverage(4.5)
            .reviewCount(5)
            .build()
    );

    // when
    boolean result = reviewOfBoardRepository.existsByMapXAndMapY(mapX, mapY);

    // then
    assertThat(result).isTrue();

  }

  @Test
  public void existByMapXAndMapY_NonExistLocation_ReturnFalse() {
    // given
    String mapX = "127.2636514";
    String mapY = "37.0323408";
    reviewOfBoardRepository.save(
        ReviewOfBoard.builder()
            .mapY(mapY)
            .mapX(mapX)
            .reviewAverage(4.5)
            .reviewCount(5)
            .build()
    );

    // when
    boolean result = reviewOfBoardRepository.existsByMapXAndMapY("999", "999");

    // then
    assertThat(result).isFalse();
  }

  @Test
  public void findAllByReviewAverageIsNotNull_ValidAverage_Success() {
    // given: null/비null 각각 1건씩 저장
    ReviewOfBoard withAvg = reviewOfBoardRepository.save(ReviewOfBoard.builder()
            .mapX("127.2636514")
            .mapY("37.0323408")
            .reviewAverage(4.5)
            .reviewCount(5)
            .build()
    );

    Pageable pageable = PageRequest.of(
        0, 2,
        Sort.by(Sort.Direction.DESC, "reviewAverage")
            .and(Sort.by(Sort.Direction.DESC, "id"))
    );

    // when
    List<BoardRankView> ranks =
        reviewOfBoardRepository.findAllByReviewAverageIsNotNull(pageable);

    // then
    assertThat(ranks).isNotNull();
    assertThat(ranks).isNotEmpty();
    // null 평균값이 포함되지 않았는지
    assertThat(ranks).allSatisfy(v -> assertThat(v.getReviewAverage()).isNotNull());

    // 우리가 넣은 4.5짜리가 포함되어 있는지(페이징이 충분히 크므로 당연 포함)
    BoardRankView top = ranks.get(0);
    assertThat(top.getReviewAverage()).isEqualTo(4.5); // Double라면 그대로 비교 가능
    assertThat(top.getMapX()).isEqualTo("127.2636514");
    assertThat(top.getMapY()).isEqualTo("37.0323408");
  }

  @Test
  public void findAllByReviewAverageIsNotNull_EmptyReview_ReturnEmpty() {
    // given
    Pageable pageable = PageRequest.of(0,5, Sort.by(Direction.DESC, "reviewAverage")
        .and(Sort.by(Direction.DESC, "id")));

    // when
    List<BoardRankView> ranks = reviewOfBoardRepository.findAllByReviewAverageIsNotNull(pageable);

    // then
    assertThat(ranks).isEmpty();
  }

}
