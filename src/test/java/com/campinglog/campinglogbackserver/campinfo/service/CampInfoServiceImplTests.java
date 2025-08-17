package com.campinglog.campinglogbackserver.campinfo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestAddReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestRemoveReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestSetReview;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetBoardReview;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetBoardReviewRank;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetMyReviewWrapper;
import com.campinglog.campinglogbackserver.campinfo.entity.Review;
import com.campinglog.campinglogbackserver.campinfo.entity.ReviewOfBoard;
import com.campinglog.campinglogbackserver.campinfo.exception.CallCampApiError;
import com.campinglog.campinglogbackserver.campinfo.exception.InvalidLimitError;
import com.campinglog.campinglogbackserver.campinfo.exception.NoExistReviewOfBoardError;
import com.campinglog.campinglogbackserver.campinfo.exception.NullReviewError;
import com.campinglog.campinglogbackserver.campinfo.repository.ReviewOfBoardRepository;
import com.campinglog.campinglogbackserver.campinfo.repository.ReviewRepository;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.exception.MemberNotFoundError;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@SpringBootTest
public class CampInfoServiceImplTests {
  @Autowired
  ReviewRepository reviewRepository;

  @Autowired
  ReviewOfBoardRepository reviewOfBoardRepository;

  @Autowired
  CampInfoServiceImpl campInfoService;

  @Test
  @Transactional
  public void getBoardReview_ValidData_Success() {
    // given
    String mapX = "126.86673078484";
    String mapY = "34.830030224221";

    // when
    ResponseGetBoardReview result = campInfoService.getBoardReview(mapX, mapY);

    // then
    assertThat(result).isNotNull();
  }

  @Test
  @Transactional
  public void getBoardReview_InvalidData_ReturnException() {
    // given
    String mapX = "999.86673078484";
    String mapY = "999.830030224221";

    // when & then
    assertThatThrownBy(() -> campInfoService.getBoardReview(mapX, mapY))
        .isInstanceOf(NoExistReviewOfBoardError.class);
  }

  @Test
  @Transactional
  public void getMyReviews_ValidData_Success() {
    // given
    String email = "taylor@taylorswift.com";

    // when
    ResponseGetMyReviewWrapper result = campInfoService.getMyReviews(email, 1, 10).block();

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isNotEmpty();

  }

  @Test
  @Transactional
  public void getMyReviews_InvalidData_ReturnEmpty() {
    // given
    String email = "none@none.com";

    // when
    ResponseGetMyReviewWrapper result = campInfoService.getMyReviews(email, 1, 10).block();

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
  }

  @Test
  @Transactional
  public void getBoardReviewRank_Success() {
    // when
    List<ResponseGetBoardReviewRank> result = campInfoService.getBoardReviewRank(4).block();

    // then
    assertThat(result).isNotEmpty();
  }

  @Test
  public void getBoardReviewRank_LimitZero_ReturnException() {
    // when & then
    assertThatThrownBy(() -> campInfoService.getBoardReviewRank(0))
        .isInstanceOf(InvalidLimitError.class);
  }

  @Test
  @Transactional
  public void addReview_Success() {
    // given
    String email = "taylor@taylorswift.com";
    String mapX = "127.2636514";
    String mapY = "37.0323408";
    double reviewScore = 4.5;

    RequestAddReview review = RequestAddReview.builder()
        .email(email)
        .mapY(mapY)
        .mapX(mapX)
        .reviewContent("굿")
        .reviewScore(reviewScore)
        .build();

    // when
    campInfoService.addReview(review);

    // then
    ReviewOfBoard reviewOfBoard = reviewOfBoardRepository.findByMapXAndMapY(mapX, mapY);
    assertThat(reviewOfBoard.getReviewCount()).isEqualTo(1);

  }

  @Test
  @Transactional
  public void addReview_MemberNotFound_ReturnException() {
    String email = "no@no.com";
    String mapX = "127.2636514";
    String mapY = "37.0323408";
    double reviewScore = 4.5;

    RequestAddReview review = RequestAddReview.builder()
        .email(email)
        .mapY(mapY)
        .mapX(mapX)
        .reviewContent("굿")
        .reviewScore(reviewScore)
        .build();

    // when & then
    assertThatThrownBy(() -> campInfoService.addReview(review))
        .isInstanceOf(MemberNotFoundError.class);
  }

  @Test
  @Transactional
  public void setReview_Success() {
    // given

    RequestSetReview setReview = RequestSetReview.builder()
        .id(1l)
        .newReviewContent("new content")
        .newReviewScore(3.0)
        .newReviewImage("new.jpg")
        .build();

    // when
    campInfoService.setReview(setReview);

    // then
    Review updated = reviewRepository.findById(setReview.getId()).orElseThrow();
    assertThat(updated.getReviewContent()).isEqualTo("new content");
    assertThat(updated.getReviewScore()).isEqualTo(3.0);
    assertThat(updated.getReviewImage()).isEqualTo("new.jpg");
  }

  @Test
  @Transactional
  public void setReview_ReviewNull_ReturnException() {
    long invalidId = 9999L;

    RequestSetReview setReview = RequestSetReview.builder()
        .id(invalidId)
        .newReviewContent("no")
        .newReviewScore(5.0)
        .newReviewImage("img.jpg")
        .build();

    // when & then
    assertThatThrownBy(() -> campInfoService.setReview(setReview))
        .isInstanceOf(NullReviewError.class);
  }


  @Test
  @Transactional
  public void removeReview_Success() {
    // given
    RequestRemoveReview removeReview = RequestRemoveReview.builder()
            .id(1L).build();

    // when
    campInfoService.removeReview(removeReview);

    // then
    assertThat(reviewRepository.findById(1L)).isEmpty();

  }

  @Test
  @Transactional
  public void removeReview_reviewNotFound_ReturnException() {
    // given
    long notExistId = 99999L;

    // when & then
    assertThatThrownBy(() ->
        campInfoService.removeReview(RequestRemoveReview.builder().id(notExistId).build()))
        .isInstanceOf(NullReviewError.class);
  }


}
