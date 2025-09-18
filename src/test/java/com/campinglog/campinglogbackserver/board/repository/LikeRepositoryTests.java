package com.campinglog.campinglogbackserver.board.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.campinglog.campinglogbackserver.board.entity.Board;
import com.campinglog.campinglogbackserver.board.entity.BoardLike;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@Slf4j
public class LikeRepositoryTests {

    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private LikeRepository likeRepository;

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

        likeRepository.save(BoardLike.builder().board(board1).member(isa).build());
    }

    @Test
    void existsByBoardAndMember_true() {
        // when
        boolean exists = likeRepository.existsByBoard_IdAndMember_Email(board1.getId(),
            "isa@test.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByBoardAndMember_false() {
        // when
        boolean diffBoard = likeRepository.existsByBoard_IdAndMember_Email(board2.getId(),
            "chaeyoung@test.com");
        boolean noMember = likeRepository.existsByBoard_IdAndMember_Email(board1.getId(),
            "none@test.com");

        // then
        assertThat(diffBoard).isFalse();
        assertThat(noMember).isFalse();
    }

    @Test
    void findByBoardAndMember_present() {
        // when
        Optional<BoardLike> opt = likeRepository.findByBoard_IdAndMember_Email(board1.getId(),
            "isa@test.com");

        // then
        assertThat(opt).isPresent();
        BoardLike like = opt.get();
        assertThat(like.getBoard().getId()).isEqualTo(board1.getId());
        assertThat(like.getMember().getEmail()).isEqualTo("isa@test.com");
    }

    @Test
    void findByBoardAndMember_empty() {
        // when
        Optional<BoardLike> opt = likeRepository.findByBoard_IdAndMember_Email(board1.getId(),
            "chaeyoung@test.com");

        // then
        assertThat(opt).isEmpty();
    }

    @Test
    void findByBoardAndMember_mismatched_empty() {
        // when
        Optional<BoardLike> opt1 = likeRepository.findByBoard_IdAndMember_Email(board2.getId(),
            "isa@test.com");
        Optional<BoardLike> opt2 = likeRepository.findByBoard_IdAndMember_Email(board1.getId(),
            "none@test.com");

        // then
        assertThat(opt1).isEmpty();
        assertThat(opt2).isEmpty();
    }

}
