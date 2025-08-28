package com.campinglog.campinglogbackserver.board.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.campinglog.campinglogbackserver.board.entity.Board;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
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
public class BoardRepositoryTests {

    @Autowired
    BoardRepository boardRepository;
    @Autowired
    MemberRepository memberRepository;

    private Member isa;
    private Member chaeyoung;

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

        boardRepository.save(
            Board.builder().boardId("board1").title("테스트").categoryName("후기").content("내용")
                .likeCount(100).viewCount(100).createdAt(
                    LocalDateTime.now().minusHours(1)).member(isa).build());

        boardRepository.save(
            Board.builder().boardId("board2").title("테스트2").categoryName("후기").content("내용2")
                .likeCount(50).viewCount(50).createdAt(LocalDateTime.now().minusHours(2))
                .member(chaeyoung).build());
    }

    @Test
    void findByBoardId_success() {
        //when
        Optional<Board> find = boardRepository.findByBoardId("board1");
        //then
        assertThat(find).isPresent();
        assertThat(find.get().getMember().getEmail()).isEqualTo("isa@test.com");
    }

    @Test
    void findByBoardId_notFound_returnEmpty() {
        //given
        String noExists = "no-such-board";

        //when
        Optional<Board> find = boardRepository.findByBoardId(noExists);

        //then
        assertThat(find).isEmpty();
    }

    @Test
    void findByMemberEmail_success_paging() {
        // when
        Page<Board> page = boardRepository.findByMemberEmail("isa@test.com", PageRequest.of(0, 10));

        // then
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getBoardId()).isEqualTo("board1");
        assertThat(page.getContent().get(0).getMember().getEmail()).isEqualTo("isa@test.com");
    }

    @Test
    void findByMemberEmail_noPosts_returnsEmpty() {
        // given
        memberRepository.save(Member.builder()
            .email("empty@test.com").password("pw").nickname("empty").name("비어있음")
            .birthday(java.time.LocalDate.of(2002, 1, 23)).phoneNumber("010-0000-0000")
            .build());

        // when
        Page<Board> page = boardRepository.findByMemberEmail("empty@test.com",
            PageRequest.of(0, 10));

        // then
        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void findByCreatedAtAfter_sorted() {
        // given
        LocalDateTime from = LocalDateTime.now().minusDays(1);

        // when
        List<Board> list = boardRepository
            .findByCreatedAtAfterOrderByLikeCountDescViewCountDescCreatedAtDesc(
                from, PageRequest.of(0, 10));

        // then
        assertThat(list).hasSize(2);
        assertThat(list).isSortedAccordingTo(
            Comparator.comparing(Board::getLikeCount).reversed()
                .thenComparing(Board::getViewCount).reversed()
                .thenComparing(Board::getCreatedAt).reversed()
        );
    }

    @Test
    void findByCreatedAtAfter_noMatch_returnsEmpty() {
        // given
        LocalDateTime from = LocalDateTime.now().plusDays(1);

        // when
        List<Board> list = boardRepository
            .findByCreatedAtAfterOrderByLikeCountDescViewCountDescCreatedAtDesc(
                from, PageRequest.of(0, 10));

        // then
        assertThat(list).isEmpty();
    }

    @Test
    void findByTitleContaining_success() {
        // when
        Page<Board> list = boardRepository
            .findByTitleContainingOrderByCreatedAtDesc("테스트", PageRequest.of(0, 10));

        // then
        assertThat(list).hasSize(2);
        assertThat(list.getContent().get(0).getCreatedAt()).isAfter(list.getContent().get(1).getCreatedAt());
    }

    @Test
    void findByTitleContaining_noMatch_returnsEmpty() {
        // given
        String keyword = "없는키워드";

        // when
        Page<Board> list = boardRepository
            .findByTitleContainingOrderByCreatedAtDesc(keyword, PageRequest.of(0, 10));

        // then
        assertThat(list).isEmpty();
    }

    @Test
    void findByCategoryName_success() {
        // when
        Page<Board> list = boardRepository
            .findByCategoryNameOrderByCreatedAtDesc("후기", PageRequest.of(0, 10));

        // then

        List<Board> content = list.getContent();

        assertThat(content).hasSize(2);
        assertThat(content.get(0).getCreatedAt()).isAfterOrEqualTo(content.get(1).getCreatedAt());
        assertThat(content).allSatisfy(b -> assertThat(b.getCategoryName()).isEqualTo("후기"));
    }

    @Test
    void findByCategoryName_noMatch_returnsEmpty() {
        // given
        String category = "공지";

        // when
        Page<Board> list = boardRepository
            .findByCategoryNameOrderByCreatedAtDesc(category, PageRequest.of(0, 10));

        // then
        assertThat(list).isEmpty();
    }


}
