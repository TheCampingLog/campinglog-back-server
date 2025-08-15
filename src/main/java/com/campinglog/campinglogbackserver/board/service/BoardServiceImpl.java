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
import com.campinglog.campinglogbackserver.board.entity.Like;
import com.campinglog.campinglogbackserver.board.repository.BoardRepository;
import com.campinglog.campinglogbackserver.board.repository.CommentRepository;
import com.campinglog.campinglogbackserver.board.repository.LikeRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final ModelMapper modelMapper;
    private final LikeRepository likeRepository;


    @Override
    public void addBoard(RequestAddBoard requestAddBoard) {
        Board board = modelMapper.map(requestAddBoard, Board.class);
        board.setBoardId(UUID.randomUUID().toString());
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
        if (requestSetBoard.getEmail() != null) {
            board.setEmail(requestSetBoard.getEmail());
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

        List<ResponseGetBoardRank> responses = modelMapper.map(boards,
            new TypeToken<List<ResponseGetBoardRank>>() {
            }.getType());
        return responses;
    }

    @Override
    public ResponseGetBoardDetail getBoardDetail(String boardId) {
        Board board = boardRepository.findByBoardId(boardId)
            .orElseThrow(() -> new EntityNotFoundException(
                "해당 boardId로 게시글을 찾을 수 없습니다. boardId=" + boardId));

        board.setViewCount(board.getViewCount() + 1);
        boardRepository.save(board);

        ResponseGetBoardDetail response = modelMapper.map(board, ResponseGetBoardDetail.class);

        return response;

    }

    @Override
    public List<ResponseGetBoardByKeyword> searchBoards(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        List<Board> boardPage = boardRepository.findByTitleContainingOrderByCreatedAtDesc(keyword,
            pageable);

        return boardPage.stream().map(board -> {
                ResponseGetBoardByKeyword response = modelMapper.map(board,
                    ResponseGetBoardByKeyword.class);
                return response;
            })
            .collect(Collectors.toList());

    }

    @Override
    public List<ResponseGetComments> getComments(String boardId, int page, int size) {
        Board board = boardRepository.findByBoardId(boardId)
            .orElseThrow(() -> new EntityNotFoundException(
                "해당 boardId로 게시글을 찾을 수 없습니다. boardId=" + boardId));

        Pageable pageable = PageRequest.of(page - 1, size);

        List<Comment> comments = commentRepository.findByBoardIdOrderByCreatedAtDesc(boardId,
            pageable);

        return comments.stream().map(comment -> modelMapper.map(comment, ResponseGetComments.class))
            .collect(Collectors.toList());
    }

    @Override
    public void updateComment(String boardId, String commentId,
        RequestSetComment requestSetComment) {
        Board board = boardRepository.findByBoardId(boardId)
            .orElseThrow(() -> new EntityNotFoundException(
                "게시글을 찾을 수 없습니다. boardId=" + boardId));

        Comment comment = commentRepository.findByCommentId(commentId)
            .orElseThrow(() -> new EntityNotFoundException(
                "댓글을 찾을 수 없습니다. commentId=" + commentId));

        if (!comment.getBoardId().equals(boardId)) {
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

        boolean alreadyLiked = likeRepository.existsByBoardIdAndEmail(boardId,
            requestAddLike.getEmail());
        if (alreadyLiked) {
            throw new RuntimeException("이미 좋아요 누른 게시글");
        }
        Like like = modelMapper.map(requestAddLike, Like.class);
        like.setBoardId(boardId);
        like.setLikeId(UUID.randomUUID().toString());
        likeRepository.save(like);

        board.setLikeCount(board.getLikeCount() + 1);
        boardRepository.save(board);
    }

    @Override
    public void addComment(String boardId, RequestAddComment requestAddComment) {
        Board board = boardRepository.findByBoardId(boardId)
            .orElseThrow(() -> new EntityNotFoundException(
                "해당 boardId로 게시글을 찾을 수 없습니다. boardId=" + boardId));
        Comment comment = modelMapper.map(requestAddComment, Comment.class);
        comment.setBoardId(boardId);
        comment.setCommentId(UUID.randomUUID().toString());
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment);

        board.setCommentCount(board.getCommentCount() + 1);
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
        Board board = boardRepository.findByBoardId(boardId)
            .orElseThrow(() -> new EntityNotFoundException(
                "게시글을 찾을 수 없습니다. boardId=" + boardId));

        Like like = likeRepository.findByBoardIdAndEmail(boardId, email)
            .orElseThrow(() -> new RuntimeException("좋아요하지 않은 게시글입니다."));

        likeRepository.delete(like);

        if (board.getLikeCount() > 0) {
            board.setLikeCount(board.getLikeCount() - 1);
            boardRepository.save(board);
        }
    }
}
