package com.campinglog.campinglogbackserver.board.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.campinglog.campinglogbackserver.board.entity.Board;
import com.campinglog.campinglogbackserver.board.entity.Comment;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@Slf4j
public class CommentRepositoryTests {

    @Autowired
    CommentRepository commentRepository;
    @Autowired
    BoardRepository boardRepository;
    @Autowired
    MemberRepository memberRepository;

    private Member isa;
    private Member chaeyoung;
    private Board board1;
    private Board board2;

    @BeforeEach
    void setUp() {
        isa = memberRepository.save(
            Member.builder().email("isa@test.com").password("password").nickname("isa").name("아이사")
                .birthday(java.time.LocalDate.of(2002, 1, 23))
                .phoneNumber("010-1111-1111")
                .build());

        chaeyoung = memberRepository.save(
            Member.builder().email("chaeyoung@test.com").password("password").nickname("chaeyoung")
                .name("이채영").birthday(java.time.LocalDate.of(2002, 1, 23))
                .phoneNumber("010-2222-2222").build());

        board1 = boardRepository.save(
            Board.builder().boardId("board1").title("테스트").categoryName("후기").content("내용")
                .likeCount(100).viewCount(100).createdAt(
                    LocalDateTime.now().minusHours(1)).member(isa).build());

        board2 = boardRepository.save(
            Board.builder().boardId("board2").title("테스트2").categoryName("후기").content("내용2")
                .likeCount(50).viewCount(50).createdAt(LocalDateTime.now().minusHours(2))
                .member(chaeyoung).build());

        commentRepository.save(
            Comment.builder()
                .commentId("c_001")
                .content("첫 댓글")
                .createdAt(LocalDateTime.now().minusHours(3))
                .board(board1)
                .member(isa)
                .build()
        );
        commentRepository.save(
            Comment.builder()
                .commentId("c_002")
                .content("두 번째 댓글")
                .createdAt(LocalDateTime.now().minusHours(2))
                .board(board1)
                .member(chaeyoung)
                .build()
        );
        commentRepository.save(
            Comment.builder()
                .commentId("c_003")
                .content("세 번째 댓글")
                .createdAt(LocalDateTime.now().minusHours(1))
                .board(board1)
                .member(isa)
                .build()
        );

    }

    @Test
    void findByCommentId_success() {
        // when
        Optional<Comment> found = commentRepository.findByCommentId("c_001");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo("첫 댓글");
    }

    @Test
    void findByCommentId_notFound_returnsEmpty() {
        // given
        String notExists = "no-such-comment";

        // when
        Optional<Comment> found = commentRepository.findByCommentId(notExists);

        // then
        assertThat(found).isEmpty();
    }


    @Test
    void findByBoardId_descOrder_paging() {
        // when
        Page<Comment> list = commentRepository.findByBoard_IdOrderByCreatedAtDescIdDesc(
            board1.getId(), PageRequest.of(0, 2));

        // then
        List<Comment> page = list.getContent();

        assertThat(page).hasSize(2);
        assertThat(page.get(0).getCommentId()).isEqualTo("c_003"); // -1h
        assertThat(page.get(1).getCommentId()).isEqualTo("c_002"); // -2h

        //then
        assertThat(page.get(0).getMember().getEmail()).isIn("isa@test.com", "chaeyoung@test.com");
    }

    @Test
    void findByBoardId_descOrder_nextPage() {
        // when
        Page<Comment> list = commentRepository.findByBoard_IdOrderByCreatedAtDescIdDesc(
            board1.getId(), PageRequest.of(1, 2));

        // then
        List<Comment> page = list.getContent();

        assertThat(page).hasSize(1);
        assertThat(page.get(0).getCommentId()).isEqualTo("c_001"); // -3h
    }

    @Test
    void findByBoardId_noComments_returnsEmpty() {
        // when
        Page<Comment> page = commentRepository.findByBoard_IdOrderByCreatedAtDescIdDesc(
            board2.getId(), PageRequest.of(0, 10));

        // then
        assertThat(page).isEmpty();
    }

    @Test
    void findByBoardId_notExistingBoard_returnsEmpty() {
        // given
        Long notExistsBoardPk = 999_999L;

        // when
        Page<Comment> page = commentRepository.findByBoard_IdOrderByCreatedAtDescIdDesc(
            notExistsBoardPk, PageRequest.of(0, 10));

        // then
        assertThat(page).isEmpty();
    }

}
