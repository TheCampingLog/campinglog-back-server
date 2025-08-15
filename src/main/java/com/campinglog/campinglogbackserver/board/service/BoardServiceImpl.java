package com.campinglog.campinglogbackserver.board.service;

import com.campinglog.campinglogbackserver.board.dto.request.RequestAddBoard;
import com.campinglog.campinglogbackserver.board.dto.request.RequestAddComment;
import com.campinglog.campinglogbackserver.board.dto.request.RequestAddLike;
import com.campinglog.campinglogbackserver.board.dto.request.RequestSetBoard;
import com.campinglog.campinglogbackserver.board.dto.request.RequestSetComment;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardByCategory;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardByKeyword;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardDetail;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardRank;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetComments;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetLike;
import com.campinglog.campinglogbackserver.board.entity.Board;
import com.campinglog.campinglogbackserver.board.entity.Comment;
import com.campinglog.campinglogbackserver.board.entity.BoardLike;
import com.campinglog.campinglogbackserver.board.repository.BoardRepository;
import com.campinglog.campinglogbackserver.board.repository.CommentRepository;
import com.campinglog.campinglogbackserver.board.repository.LikeRepository;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;

    @Override
    public void addBoard(RequestAddBoard requestAddBoard) {
        Member member = memberRepository.findById(requestAddBoard.getEmail())
            .orElseThrow(() -> new EntityNotFoundException(
                "회원을 찾을 수 없습니다. email=" + requestAddBoard.getEmail()));

        Board board = modelMapper.map(requestAddBoard, Board.class);
        board.setBoardId(UUID.randomUUID().toString());
        board.setMember(member);
        board.setViewCount(0);
        board.setLikeCount(0);
        board.setCommentCount(0);

        boardRepository.save(board);
    }

    @Override
    public void setBoard(RequestSetBoard requestSetBoard) {
        if (requestSetBoard.getBoardId() == null || requestSetBoard.getBoardId().isBlank()) {
            throw new IllegalArgumentException("boardId는 필수입니다.");
        }

        Board board = boardRepository.findByBoardId(requestSetBoard.getBoardId())
            .orElseThrow(() -> new EntityNotFoundException(
                "해당 boardId로 게시글을 찾을 수 없습니다. boardId=" + requestSetBoard.getBoardId()));

        if (!board.getMember().getEmail().equals(requestSetBoard.getEmail())) {
            throw new RuntimeException("본인의 게시글만 수정할 수 있습니다.");
        }

        if (requestSetBoard.getTitle() != null) {
            board.setTitle(requestSetBoard.getTitle());
        }
        if (requestSetBoard.getContent() != null) {
            board.setContent(requestSetBoard.getContent());
        }
        if (requestSetBoard.getCategoryName() != null) {
            board.setCategoryName(requestSetBoard.getCategoryName());
        }
        if (requestSetBoard.getBoardImage() != null) {
            board.setBoardImage(requestSetBoard.getBoardImage());
        }

        boardRepository.save(board);
    }

    @Override
    public void deleteBoard(String boardId) {
        Board board = boardRepository.findByBoardId(boardId)
            .orElseThrow(() -> new EntityNotFoundException(
                "해당 boardId로 게시글을 찾을 수 없습니다. boardId=" + boardId));

        boardRepository.delete(board);

    }

    @Override
    public List<ResponseGetBoardRank> getBoardRank(int limit) {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        Pageable pageable = PageRequest.of(0, limit);

        List<Board> boards = boardRepository.findByCreatedAtAfterOrderByLikeCountDescViewCountDescCreatedAtDesc(
            weekAgo, pageable);

        return boards.stream()
            .map(board -> {
                ResponseGetBoardRank response = modelMapper.map(board, ResponseGetBoardRank.class);
                response.setNickname(board.getMember().getNickname());
                return response;
            })
            .collect(Collectors.toList());
    }

    @Override
    public ResponseGetBoardDetail getBoardDetail(String boardId) {
        Board board = boardRepository.findByBoardId(boardId)
            .orElseThrow(() -> new EntityNotFoundException(
                "해당 boardId로 게시글을 찾을 수 없습니다. boardId=" + boardId));

        board.setViewCount(board.getViewCount() + 1);
        boardRepository.save(board);

        ResponseGetBoardDetail response = modelMapper.map(board, ResponseGetBoardDetail.class);
        response.setNickname(board.getMember().getNickname());
        response.setEmail(board.getMember().getEmail());

        return response;
    }

    @Override
    public List<ResponseGetBoardByKeyword> searchBoards(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        List<Board> boards = boardRepository.findByTitleContainingOrderByCreatedAtDesc(keyword,
            pageable);

        return boards.stream()
            .map(board -> {
                ResponseGetBoardByKeyword response = modelMapper.map(board,
                    ResponseGetBoardByKeyword.class);
                response.setNickName(board.getMember().getNickname());
                return response;
            })
            .collect(Collectors.toList());
    }

    @Override
    public void addComment(String boardId, RequestAddComment requestAddComment) {
        Board board = boardRepository.findByBoardId(boardId)
            .orElseThrow(() -> new EntityNotFoundException(
                "해당 boardId로 게시글을 찾을 수 없습니다. boardId=" + boardId));

        Member member = memberRepository.findByEmail(requestAddComment.getEmail())
            .orElseThrow(() -> new EntityNotFoundException(
                "회원을 찾을 수 없습니다. nickname=" + requestAddComment.getEmail()));

        Comment comment = modelMapper.map(requestAddComment, Comment.class);
        comment.setCommentId(UUID.randomUUID().toString());
        comment.setBoard(board);
        comment.setMember(member);

        commentRepository.save(comment);

        board.setCommentCount(board.getCommentCount() + 1);
        boardRepository.save(board);


    }

    @Override
    public List<ResponseGetComments> getComments(String boardId, int page, int size) {
        Board board = boardRepository.findByBoardId(boardId)
            .orElseThrow(() -> new EntityNotFoundException(
                "해당 boardId로 게시글을 찾을 수 없습니다. boardId=" + boardId));

        Pageable pageable = PageRequest.of(page - 1, size);

        List<Comment> comments = commentRepository.findByBoardBoardIdOrderByCreatedAtDesc(boardId,
            pageable);

        return comments.stream()
            .map(comment -> {
                ResponseGetComments response = modelMapper.map(comment, ResponseGetComments.class);
                response.setNickname(comment.getMember().getNickname());
                return response;
            })
            .collect(Collectors.toList());
    }

    @Override
    public void updateComment(String boardId, String commentId,
        RequestSetComment requestSetComment) {
        Comment comment = commentRepository.findByCommentId(commentId)
            .orElseThrow(() -> new EntityNotFoundException(
                "댓글을 찾을 수 없습니다. commentId=" + commentId));

        if (!comment.getBoard().getBoardId().equals(boardId)) {
            throw new RuntimeException("해당 게시글의 댓글이 아닙니다.");
        }

        comment.setContent(requestSetComment.getContent());
        commentRepository.save(comment);


    }

    @Override
    public void deleteComment(String boardId, String commentId) {
        Comment comment = commentRepository.findByCommentId(commentId)
            .orElseThrow(() -> new EntityNotFoundException(
                "댓글을 찾을 수 없습니다. commentId=" + commentId));

        Board board = comment.getBoard();

        if (!board.getBoardId().equals(boardId)) {
            throw new RuntimeException("해당 게시글의 댓글이 아닙니다.");
        }

        commentRepository.delete(comment);

        if (board.getCommentCount() > 0) {
            board.setCommentCount(board.getCommentCount() - 1);
            boardRepository.save(board);
        }


    }

    @Override
    public void addLike(String boardId, RequestAddLike requestAddLike) {

        Board board = boardRepository.findByBoardId(boardId)
            .orElseThrow(() -> new EntityNotFoundException(
                "해당 boardId로 게시글을 찾을 수 없습니다. boardId=" + boardId));

        Member member = memberRepository.findById(requestAddLike.getEmail())
            .orElseThrow(() -> new EntityNotFoundException(
                "회원을 찾을 수 없습니다. email=" + requestAddLike.getEmail()));

        boolean alreadyLiked = likeRepository.existsByBoardBoardIdAndMemberEmail(
            boardId, requestAddLike.getEmail());

        if (alreadyLiked) {
            throw new RuntimeException("이미 좋아요 누른 게시글");
        }

        BoardLike boardLike = modelMapper.map(requestAddLike, BoardLike.class);
        boardLike.setLikeId(UUID.randomUUID().toString());
        boardLike.setBoard(board);
        boardLike.setMember(member);

        likeRepository.save(boardLike);

        board.setLikeCount(board.getLikeCount() + 1);
        boardRepository.save(board);


    }

    @Override
    public List<ResponseGetBoardByCategory> getBoardsByCategory(String category, int page,
        int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        List<Board> boards = boardRepository.findByCategoryNameOrderByCreatedAtDesc(category,
            pageable);

        return boards.stream()
            .map(board -> {
                ResponseGetBoardByCategory response = modelMapper.map(board,
                    ResponseGetBoardByCategory.class);
                response.setNickName(board.getMember().getNickname());
                return response;
            })
            .collect(Collectors.toList());
    }

    @Override
    public ResponseGetLike getLikes(String boardId) {
        Board board = boardRepository.findByBoardId(boardId)
            .orElseThrow(() -> new EntityNotFoundException(
                "해당 boardId로 게시글을 찾을 수 없습니다. boardId=" + boardId));

        ResponseGetLike response = modelMapper.map(board, ResponseGetLike.class);
        response.setBoardId(boardId);
        response.setLikeCount(board.getLikeCount());
        return response;
    }

    @Override
    public void deleteLike(String boardId, String email) {
        BoardLike boardLike = likeRepository.findByBoardBoardIdAndMemberEmail(boardId, email)
            .orElseThrow(() -> new RuntimeException("좋아요하지 않은 게시글입니다."));

        Board board = boardLike.getBoard();

        likeRepository.delete(boardLike);

        if (board.getLikeCount() > 0) {
            board.setLikeCount(board.getLikeCount() - 1);
            boardRepository.save(board);
        }

    }
}