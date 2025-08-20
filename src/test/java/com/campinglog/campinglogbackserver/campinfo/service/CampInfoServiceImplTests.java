package com.campinglog.campinglogbackserver.campinfo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestAddReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestRemoveReview;
import com.campinglog.campinglogbackserver.campinfo.dto.request.RequestSetReview;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetBoardReview;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetBoardReviewRank;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampByKeyword;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampDetail;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetCampListLatest;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetMyReviewWrapper;
import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetReviewListPage;
import com.campinglog.campinglogbackserver.campinfo.entity.Review;
import com.campinglog.campinglogbackserver.campinfo.entity.ReviewOfBoard;
import com.campinglog.campinglogbackserver.campinfo.exception.ApiParsingError;
import com.campinglog.campinglogbackserver.campinfo.exception.CallCampApiError;
import com.campinglog.campinglogbackserver.campinfo.exception.InvalidLimitError;
import com.campinglog.campinglogbackserver.campinfo.exception.NoExistCampError;
import com.campinglog.campinglogbackserver.campinfo.exception.NoExistReviewOfBoardError;
import com.campinglog.campinglogbackserver.campinfo.exception.NoSearchResultError;
import com.campinglog.campinglogbackserver.campinfo.exception.NullReviewError;
import com.campinglog.campinglogbackserver.campinfo.repository.ReviewOfBoardRepository;
import com.campinglog.campinglogbackserver.campinfo.repository.ReviewRepository;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.exception.MemberNotFoundError;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
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

  @Test
  @Transactional
  @org.springframework.test.context.jdbc.Sql(statements = {
      "DELETE FROM reviews",
      "DELETE FROM review_of_board",
      "DELETE FROM members",
      "INSERT INTO members (email, name, nickname, password, phone_number, birthday, join_date, member_grade, role) " +
          "VALUES ('taylor@taylorswift.com','Taylor','taylor','pw','010-0000-0000','1990-01-01','2025-08-01','GREEN','USER')"
  }, executionPhase = org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  public void getReviewList_Success() {
    // given
    String email = "taylor@taylorswift.com";
    String mapX = "129.2636514";
    String mapY = "35.0323408";

    reviewRepository.save(Review.builder()
        .member(Member.builder().email(email).build())
        .mapX(mapX)
        .mapY(mapY)
        .reviewContent("첫 리뷰")
        .reviewScore(4.5)
        .reviewImage("img1.png")
        .build());
    int page = 0;
    int size = 10;

    // when
    ResponseGetReviewListPage result = campInfoService.getReviewList(mapX, mapY, page, size);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
  }


  @Test
  @Transactional
  public void getReviewList_NoReview_ReturnEmpty() {
    // given
    String mapX = "130.00";
    String mapY = "32.10";

    // when
    ResponseGetReviewListPage result = campInfoService.getReviewList(mapX, mapY, 0, 5);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();

  }

  private WebClient stubWebClient(String body, HttpStatus status) {
    ExchangeFunction fx = request ->
        Mono.just(
            ClientResponse.create(status)
                .header("Content-Type", "application/json")
                .body(body)
                .build()
        );
    return WebClient.builder().exchangeFunction(fx).build();
  }

  @Test
  public void getCampDetail_Success() {
    // given
    String mapX = "127.2636514";
    String mapY = "37.0323408";

    // when
    ResponseGetCampDetail result = campInfoService.getCampDetail(mapX, mapY).block();

    // then
    assertThat(result).isNotNull();
    assertThat(result.getFacltNm()).isNotEmpty();

  }

  @Test
  public void getCampDetail_LocationNotFound_ReturnException() {
    // given
    String mapX = "999.2636514";
    String mapY = "999.0323408";

    // when & then
    assertThatThrownBy(() -> campInfoService.getCampDetail(mapX, mapY).block())
        .isInstanceOf(NoExistCampError.class);
  }


  @Test
  public void getCampListLatest_Success() {
    // given
    int pageNo = 0;

    // when
    List<ResponseGetCampListLatest> result = campInfoService.getCampListLatest(pageNo).block();

    // then
    assertThat(result).isNotNull();
    assertThat(result).isNotEmpty();
  }

  @Test
  public void getCampListLatest_ParseError_ReturnException() {
    // given
    String invalidJson = "{invalid json}";
    WebClient webClient = stubWebClient(invalidJson, HttpStatus.OK);

    CampInfoServiceImpl service = new CampInfoServiceImpl(null, null, webClient, new ObjectMapper(), null);

    // when & then
    assertThatThrownBy(() ->
        service.getCampListLatest(1).block())
        .isInstanceOf(ApiParsingError.class);
  }

  @Test
  public void getCampByKeyword_Success() {
    // given
    String keyword = "휴양림";
    int pageNo = 0;

    // when
    List<ResponseGetCampByKeyword> result = campInfoService.getCampByKeyword(keyword, pageNo).block();

    // then
    assertThat(result).isNotNull();
    assertThat(result.get(0).getFacltNm()).contains("휴양림");
  }

  @Test
  public void getCampByKeyword_NoResult_ReturnException() {
    // given
    String keyword = "헬스";
    int pageNo = 0;

    // when & then
    assertThatThrownBy(() -> campInfoService.getCampByKeyword(keyword, pageNo).block())
        .isInstanceOf(NoSearchResultError.class);
  }

}
