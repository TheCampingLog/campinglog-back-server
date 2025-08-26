package com.campinglog.campinglogbackserver.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campinglog.campinglogbackserver.board.dto.request.RequestAddBoard;
import com.campinglog.campinglogbackserver.board.dto.request.RequestAddComment;
import com.campinglog.campinglogbackserver.board.dto.request.RequestAddLike;
import com.campinglog.campinglogbackserver.board.dto.request.RequestSetBoard;
import com.campinglog.campinglogbackserver.board.dto.request.RequestSetComment;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardByCategory;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardByCategoryWrapper;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardByKeyword;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardDetail;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardRank;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetComments;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetCommentsWrapper;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetLike;
import com.campinglog.campinglogbackserver.board.entity.Board;
import com.campinglog.campinglogbackserver.board.entity.Comment;
import com.campinglog.campinglogbackserver.board.exception.AlreadyLikedError;
import com.campinglog.campinglogbackserver.board.exception.BoardNotFoundError;
import com.campinglog.campinglogbackserver.board.exception.CommentNotFoundError;
import com.campinglog.campinglogbackserver.board.exception.InvalidBoardRequestError;
import com.campinglog.campinglogbackserver.board.exception.NotYourBoardError;
import com.campinglog.campinglogbackserver.board.repository.BoardRepository;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.exception.MemberNotFoundError;
import com.campinglog.campinglogbackserver.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
public class BoardServiceImplTests {

    @Autowired
    BoardService boardService;
    @Autowired
    BoardRepository boardRepository;
    @Autowired
    MemberRepository memberRepository;
    private Member m1, m2;
    private String boardAId;
    private String boardBId;

    private Member member(String email, String nickname, String name) {
        return Member.builder()
            .email(email)
            .password("password")
            .nickname(nickname)
            .name(name)
            .birthday(LocalDate.of(2000, 1, 1))
            .phoneNumber("010-0000-0000")
            .build();
    }

    @BeforeEach
    void setUp() {
        // given: 회원 2명
        m1 = memberRepository.save(member("isa@test.com", "STAYC-ISA", "아이사"));
        m2 = memberRepository.save(member("chaeyoung@test.com", "chaeyoung", "이채영"));

        Board savedA = boardService.addBoard(RequestAddBoard.builder()
            .title("자바 캠핑 팁").content("내용A").categoryName("후기")
            .email(m1.getEmail()).build());

        Board savedB = boardService.addBoard(RequestAddBoard.builder()
            .title("봄철 장비 추천").content("내용B").categoryName("정보")
            .email(m2.getEmail()).build());

        boardAId = savedA.getBoardId();
        boardBId = savedB.getBoardId();

        boardRepository.findByBoardId(boardAId).ifPresent(board -> {
            board.setCreatedAt(LocalDateTime.now().minusHours(1));
            boardRepository.save(board);
        });
        boardRepository.findByBoardId(boardBId).ifPresent(board -> {
            board.setCreatedAt(LocalDateTime.now().minusHours(2));
            boardRepository.save(board);
        });

        assertThat(boardAId).isNotBlank();
        assertThat(boardBId).isNotBlank();
    }

    @Test
    void addBoard_success() {
        Board saved = boardService.addBoard(RequestAddBoard.builder()
            .title("새제목").content("새내용").categoryName("후기")
            .email(m1.getEmail()).build());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBoardId()).isNotBlank();
        assertThat(saved.getMember().getEmail()).isEqualTo(m1.getEmail());
        assertThat(saved.getViewCount()).isZero();
        assertThat(saved.getLikeCount()).isZero();
        assertThat(saved.getCommentCount()).isZero();
    }

    @Test
    void addBoard_noMember_throws() {
        assertThatThrownBy(() -> boardService.addBoard(RequestAddBoard.builder()
            .title("제목").content("내용").categoryName("후기")
            .email("no@test.com").build()))
            .isInstanceOf(MemberNotFoundError.class);
    }


    @Test
    void setBoard_blankBoardId_throws() {
        assertThatThrownBy(() -> boardService.setBoard(RequestSetBoard.builder()
            .boardId(" ").email(m1.getEmail()).title("x").build()))
            .isInstanceOf(InvalidBoardRequestError.class);
    }

    @Test
    void setBoard_notFound_throws() {
        assertThatThrownBy(() -> boardService.setBoard(RequestSetBoard.builder()
            .boardId("NOPE").email(m1.getEmail()).title("x").build()))
            .isInstanceOf(BoardNotFoundError.class);
    }

    @Test
    void setBoard_ownerOnly() {
        // when
        boardService.setBoard(RequestSetBoard.builder()
            .boardId(boardAId).email(m1.getEmail())
            .title("수정제목").content("수정내용").categoryName("정보").boardImage("a.png")
            .build());

        // then
        Board after = boardRepository.findByBoardId(boardAId).orElseThrow();
        assertThat(after.getTitle()).isEqualTo("수정제목");
        assertThat(after.getCategoryName()).isEqualTo("정보");
        assertThat(after.getBoardImage()).isEqualTo("a.png");

        // when & then
        assertThatThrownBy(() -> boardService.setBoard(RequestSetBoard.builder()
            .boardId(boardAId).email(m2.getEmail()).title("해킹").build()))
            .isInstanceOf(NotYourBoardError.class);
    }

    @Test
    void getBoardDetail_increaseView() {
        long before = boardRepository.findByBoardId(boardAId).orElseThrow().getViewCount();

        ResponseGetBoardDetail dto = boardService.getBoardDetail(boardAId);

        Board after = boardRepository.findByBoardId(boardAId).orElseThrow();
        assertThat(after.getViewCount()).isEqualTo(before + 1);
        assertThat(dto.getBoardId()).isEqualTo(boardAId);
        assertThat(dto.getEmail()).isEqualTo(m1.getEmail());
        assertThat(dto.getNickname()).isEqualTo(m1.getNickname());
    }

    @Test
    void getBoardDetail_notFound_throws() {
        assertThatThrownBy(() -> boardService.getBoardDetail("NOPE"))
            .isInstanceOf(BoardNotFoundError.class);
    }

    @Test
    void deleteBoard_success() {
        boardService.deleteBoard(boardBId);
        assertThat(boardRepository.findByBoardId(boardBId)).isEmpty();
    }

    @Test
    void deleteBoard_notFound_throws() {
        assertThatThrownBy(() -> boardService.deleteBoard("NOPE"))
            .isInstanceOf(BoardNotFoundError.class);
    }

    @Test
    void getBoardRank_weekTopN() {
        boardService.getBoardDetail(boardAId);
        boardService.getBoardDetail(boardAId);
        boardService.addLike(boardAId, RequestAddLike.builder().email(m1.getEmail()).build());
        boardService.addLike(boardAId, RequestAddLike.builder().email(m2.getEmail()).build());

        List<ResponseGetBoardRank> rank = boardService.getBoardRank(10);

        assertThat(rank).isNotEmpty();
        assertThat(rank.get(0).getBoardId()).isEqualTo(boardAId);
        assertThat(rank.get(0).getNickname()).isEqualTo(m1.getNickname());
    }

    @Test
    void searchBoards_byKeyword_latest() {
        List<ResponseGetBoardByKeyword> res = boardService.searchBoards("장비", 1, 10);
        assertThat(res).extracting(ResponseGetBoardByKeyword::getBoardId)
            .containsExactly(boardBId);
    }

    @Test
    void getBoardsByCategory_latest() {
        ResponseGetBoardByCategoryWrapper res = boardService.getBoardsByCategory("후기", 1, 10);
        assertThat(res.getContent()).extracting(ResponseGetBoardByCategory::getBoardId)
            .containsExactly(boardAId);
    }

    @Test
    void add_update_delete_comment_and_count() {
        // add
        Comment saved = boardService.addComment(boardAId, RequestAddComment.builder()
            .content("원본").email(m2.getEmail()).build());
        String savedCommentId = saved.getCommentId();

        Board afterAdd = boardRepository.findByBoardId(boardAId).orElseThrow();
        assertThat(afterAdd.getCommentCount()).isEqualTo(1L);

        // update
        boardService.updateComment(boardAId, savedCommentId,
            RequestSetComment.builder().content("수정").build());
        ResponseGetCommentsWrapper wrapper = boardService.getComments(boardAId, 1, 10);

        List<ResponseGetComments> list = wrapper.getContent();

        assertThat(list).anySatisfy(c -> {
            if (c.getCommentId().equals(savedCommentId)) {
                assertThat(c.getContent()).isEqualTo("수정");
            }
        });

        // delete
        boardService.deleteComment(boardAId, savedCommentId);
        Board afterDel = boardRepository.findByBoardId(boardAId).orElseThrow();
        assertThat(afterDel.getCommentCount()).isZero();
    }

    @Test
    void addComment_boardNotFound_throws() {
        assertThatThrownBy(() -> boardService.addComment("NOPE",
            RequestAddComment.builder().content("x").email(m1.getEmail()).build()))
            .isInstanceOf(BoardNotFoundError.class);
    }

    @Test
    void addComment_memberNotFound_throws() {
        assertThatThrownBy(() -> boardService.addComment(boardAId,
            RequestAddComment.builder().content("x").email("no@test.com").build()))
            .isInstanceOf(MemberNotFoundError.class);
    }

    @Test
    void updateComment_notFound_throws() {
        assertThatThrownBy(() -> boardService.updateComment(boardAId, "NOPE",
            RequestSetComment.builder().content("x").build()))
            .isInstanceOf(CommentNotFoundError.class);
    }

    @Test
    void getComments_order_desc_with_tiebreak() {
        boardService.addComment(boardAId, RequestAddComment.builder()
            .content("1").email(m1.getEmail()).build());
        boardService.addComment(boardAId, RequestAddComment.builder()
            .content("2").email(m1.getEmail()).build());
        boardService.addComment(boardAId, RequestAddComment.builder()
            .content("3").email(m1.getEmail()).build());

        ResponseGetCommentsWrapper wrapper1 = boardService.getComments(boardAId, 1, 2);
        ResponseGetCommentsWrapper wrapper2 = boardService.getComments(boardAId, 2, 2);

        List<ResponseGetComments> page1 = wrapper1.getContent();
        List<ResponseGetComments> page2 = wrapper2.getContent();

        assertThat(page1).hasSize(2);
        assertThat(page2).hasSize(1);
        assertThat(page1.get(0).getContent()).isEqualTo("3");
        assertThat(page1.get(1).getContent()).isEqualTo("2");
        assertThat(page2.get(0).getContent()).isEqualTo("1");
    }

    @Test
    void updateComment_wrongBoard_throws() {
        var saved = boardService.addComment(boardAId,
            RequestAddComment.builder().content("x").email(m1.getEmail()).build());

        assertThatThrownBy(() -> boardService.updateComment(boardBId, saved.getCommentId(),
            RequestSetComment.builder().content("y").build()))
            .isInstanceOf(NotYourBoardError.class);
    }

    @Test
    void deleteComment_notFound_throws() {
        assertThatThrownBy(() -> boardService.deleteComment(boardAId, "NOPE"))
            .isInstanceOf(CommentNotFoundError.class);
    }

    @Test
    void add_like_then_duplicate_then_delete() {
        long before = boardRepository.findByBoardId(boardAId).orElseThrow().getLikeCount();

        boardService.addLike(boardAId, RequestAddLike.builder().email(m2.getEmail()).build());
        Board afterLike = boardRepository.findByBoardId(boardAId).orElseThrow();
        assertThat(afterLike.getLikeCount()).isEqualTo(before + 1);

        assertThatThrownBy(() ->
            boardService.addLike(boardAId, RequestAddLike.builder().email(m2.getEmail()).build()))
            .isInstanceOf(AlreadyLikedError.class);

        boardService.deleteLike(boardAId, m2.getEmail());
        Board afterDel = boardRepository.findByBoardId(boardAId).orElseThrow();
        assertThat(afterDel.getLikeCount()).isEqualTo(before);
    }

    @Test
    void getLikes_returns_current_count() {
        ResponseGetLike res = boardService.getLikes(boardAId);
        assertThat(res.getBoardId()).isEqualTo(boardAId);
        assertThat(res.getLikeCount())
            .isEqualTo(boardRepository.findByBoardId(boardAId).orElseThrow().getLikeCount());
    }
}



