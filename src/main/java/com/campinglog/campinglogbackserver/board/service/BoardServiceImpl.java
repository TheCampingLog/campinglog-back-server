package com.campinglog.campinglogbackserver.board.service;

import com.campinglog.campinglogbackserver.board.dto.request.RequestAddBoard;
import com.campinglog.campinglogbackserver.board.dto.request.RequestAddComment;
import com.campinglog.campinglogbackserver.board.dto.request.RequestAddLike;
import com.campinglog.campinglogbackserver.board.dto.request.RequestSetBoard;
import com.campinglog.campinglogbackserver.board.dto.request.RequestSetComment;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardByCategory;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardByCategoryWrapper;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardByKeyword;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardByKeywordWrapper;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardDetail;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardRank;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetComments;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetCommentsWrapper;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetLike;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseToggleLike;
import com.campinglog.campinglogbackserver.board.entity.Board;
import com.campinglog.campinglogbackserver.board.entity.BoardLike;
import com.campinglog.campinglogbackserver.board.entity.Comment;
import com.campinglog.campinglogbackserver.board.exception.AlreadyLikedError;
import com.campinglog.campinglogbackserver.board.exception.BoardNotFoundError;
import com.campinglog.campinglogbackserver.board.exception.CommentNotFoundError;
import com.campinglog.campinglogbackserver.board.exception.InvalidBoardRequestError;
import com.campinglog.campinglogbackserver.board.exception.NotLikedError;
import com.campinglog.campinglogbackserver.board.exception.NotYourBoardError;
import com.campinglog.campinglogbackserver.board.repository.BoardRepository;
import com.campinglog.campinglogbackserver.board.repository.CommentRepository;
import com.campinglog.campinglogbackserver.board.repository.LikeRepository;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.exception.MemberNotFoundError;
import com.campinglog.campinglogbackserver.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private Board getBoardOrThrow(String boardId) {
        return boardRepository.findByBoardId(boardId)
            .orElseThrow(() -> new BoardNotFoundError(
                "해당 boardId로 게시글을 찾을 수 없습니다. boardId=" + boardId));
    }

    private Long getBoardPkOrThrow(String boardId) {
        return getBoardOrThrow(boardId).getId();
    }

    private Member getMemberOrThrow(String email) {
        return memberRepository.findById(email)
            .orElseThrow(() -> new MemberNotFoundError("회원을 찾을 수 없습니다. email=" + email));
    }

    @Override
    public Board addBoard(RequestAddBoard requestAddBoard) {
        Member member = getMemberOrThrow(requestAddBoard.getEmail());

        Board board = modelMapper.map(requestAddBoard, Board.class);
        board.setMember(member);
        board.setViewCount(0);
        board.setLikeCount(0);
        board.setCommentCount(0);

        return boardRepository.save(board);
    }

    @Override
    public void setBoard(RequestSetBoard requestSetBoard) {
        if (requestSetBoard.getBoardId() == null || requestSetBoard.getBoardId().isBlank()) {
            throw new InvalidBoardRequestError("boardId는 필수입니다.");
        }

        Board board = getBoardOrThrow(requestSetBoard.getBoardId());
        if (!board.getMember().getEmail().equals(requestSetBoard.getEmail())) {
            throw new NotYourBoardError("본인의 게시글만 수정할 수 있습니다.");
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
        Board board = getBoardOrThrow(boardId);
        boardRepository.delete(board);
    }

    @Override
    public List<ResponseGetBoardRank> getBoardRank(int limit) {
        if (limit < 1) {
            throw new InvalidBoardRequestError("limit는 1 이상이어야 합니다.");
        }
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        Pageable pageable = PageRequest.of(0, limit);

        Page<Board> boards = boardRepository.findByCreatedAtAfter(
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
    public ResponseGetBoardDetail getBoardDetail(String boardId, String userEmail) {
        Board board = getBoardOrThrow(boardId);

        board.setViewCount(board.getViewCount() + 1);
        boardRepository.save(board);

        boolean isLiked = false;
        if (userEmail != null && !userEmail.isBlank()) {
            isLiked = likeRepository.existsByBoard_IdAndMember_Email(board.getId(), userEmail);
        }

        ResponseGetBoardDetail response = modelMapper.map(board, ResponseGetBoardDetail.class);
        response.setNickname(board.getMember().getNickname());
        response.setEmail(board.getMember().getEmail());
        response.setLiked(isLiked);
        return response;
    }

    @Override
    public ResponseGetBoardByKeywordWrapper searchBoards(String keyword, String category, int page,
        int size) {
        if (page < 1 || size < 1) {
            throw new InvalidBoardRequestError("page>=1, size>=1 이어야 합니다.");
        }

        Pageable pageable = PageRequest.of(page - 1, size,
            Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Board> boards = boardRepository.findByCategoryNameAndTitleContainingIgnoreCase(
            category.trim(),
            keyword.trim(),
            pageable
        );

        List<ResponseGetBoardByKeyword> dtoList = boards.getContent().stream()
            .map(b -> modelMapper.map(b, ResponseGetBoardByKeyword.class))
            .collect(Collectors.toList());

        return ResponseGetBoardByKeywordWrapper.builder()
            .content(dtoList)
            .pageNumber(boards.getNumber())
            .pageSize(boards.getSize())
            .totalPages(boards.getTotalPages())
            .isFirst(boards.isFirst())
            .isLast(boards.isLast())
            .build();


    }

    @Override
    public Comment addComment(String boardId, RequestAddComment requestAddComment) {
        Board board = getBoardOrThrow(boardId);
        Member member = memberRepository.findByEmail(requestAddComment.getEmail())
            .orElseThrow(() -> new MemberNotFoundError(
                "회원을 찾을 수 없습니다. email=" + requestAddComment.getEmail()));

        Comment comment = modelMapper.map(requestAddComment, Comment.class);
        comment.setBoard(board);
        comment.setMember(member);

        Comment saved = commentRepository.save(comment);

        board.setCommentCount(board.getCommentCount() + 1);
        boardRepository.save(board);

        return saved;

    }

    @Override
    public ResponseGetCommentsWrapper getComments(String boardId, int page, int size) {
        log.info("[Service] getComments 시작. 전달받은 page={}, size={}", page, size);
        if (page < 1 || size < 1) {
            throw new InvalidBoardRequestError("page>=1, size>=1 이어야 합니다.");
        }
        Long boardIdPk = getBoardPkOrThrow(boardId);
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Comment> comments = commentRepository.findByBoard_IdOrderByCreatedAtDescIdDesc(
            boardIdPk,
            pageable);

        List<ResponseGetComments> dtoList = comments.getContent().stream()
            .map(comment -> {
                ResponseGetComments
                    response = modelMapper.map(comment, ResponseGetComments.class);
                response.setNickname(comment.getMember().getNickname());
                response.setCreatedAt(comment.getCreatedAt().toString());
                return response;
            })
            .collect(Collectors.toList());
        return ResponseGetCommentsWrapper.builder()
            .content(dtoList)
            .pageNumber(comments.getNumber())
            .pageSize(comments.getSize())
            .totalPages(comments.getTotalPages())
            .totalComments((int) comments.getTotalElements()) // long을 int로 형변환
            .isFirst(comments.isFirst())
            .isLast(comments.isLast())
            .build();
    }

    @Override
    public void updateComment(String boardId, String commentId,
        RequestSetComment requestSetComment) {
        Long boardIdPk = getBoardPkOrThrow(boardId);

        Comment comment = commentRepository.findByCommentId(commentId)
            .orElseThrow(() -> new CommentNotFoundError(
                "댓글을 찾을 수 없습니다. commentId=" + commentId));

        if (!comment.getBoard().getId().equals(boardIdPk)) {
            throw new NotYourBoardError("해당 게시글의 댓글이 아닙니다.");
        }

        comment.setContent(requestSetComment.getContent());
        commentRepository.save(comment);
    }

    @Override
    public void deleteComment(String boardId, String commentId) {
        Long boardIdPk = getBoardPkOrThrow(boardId);

        Comment comment = commentRepository.findByCommentId(commentId)
            .orElseThrow(() -> new CommentNotFoundError(
                "댓글을 찾을 수 없습니다. commentId=" + commentId));

        if (!comment.getBoard().getId().equals(boardIdPk)) {
            throw new NotYourBoardError("해당 게시글의 댓글이 아닙니다.");
        }

        commentRepository.delete(comment);

        Board board = comment.getBoard();
        if (board.getCommentCount() > 0) {
            board.setCommentCount(board.getCommentCount() - 1);
            boardRepository.save(board);
        }

    }

    @Override
    public ResponseToggleLike addLike(String boardId, RequestAddLike requestAddLike) {

        Board board = getBoardOrThrow(boardId);
        Member member = getMemberOrThrow(requestAddLike.getEmail());

        if (likeRepository.existsByBoard_IdAndMember_Email(board.getId(),
            requestAddLike.getEmail())) {
            throw new AlreadyLikedError("이미 좋아요 누른 게시글");
        }

        BoardLike boardLike = BoardLike.builder().board(board).member(member).build();
        likeRepository.save(boardLike);

        board.setLikeCount(board.getLikeCount() + 1);

        return ResponseToggleLike.builder()
            .isLiked(true)
            .likeCount(board.getLikeCount())
            .build();
    }

    @Override
    public ResponseGetBoardByCategoryWrapper getBoardsByCategory(String category, int page,
        int size) {
        if (page < 1 || size < 1) {
            throw new InvalidBoardRequestError("page>=1, size>=1 이어야 합니다.");
        }
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Board> boardPage = boardRepository.findByCategoryName(category,
            pageable);

        List<ResponseGetBoardByCategory> dtoList = boardPage.getContent().stream()
            .map(board -> {
                ResponseGetBoardByCategory response = modelMapper.map(board,
                    ResponseGetBoardByCategory.class);
                return response;
            })
            .collect(Collectors.toList());

        return ResponseGetBoardByCategoryWrapper.builder()
            .content(dtoList)
            .totalPages(boardPage.getTotalPages())
            .totalElements(boardPage.getTotalElements())
            .pageNumber(boardPage.getNumber())
            .pageSize(boardPage.getSize())
            .isFirst(boardPage.isFirst())
            .isLast(boardPage.isLast())
            .build();
    }

    @Override
    public ResponseGetLike getLikes(String boardId) {
        Board board = getBoardOrThrow(boardId);

        ResponseGetLike response = modelMapper.map(board, ResponseGetLike.class);
        response.setBoardId(boardId);
        response.setLikeCount(board.getLikeCount());
        return response;
    }

    @Override
    public ResponseToggleLike deleteLike(String boardId, String email) {
        BoardLike boardLike = likeRepository.findByBoard_IdAndMember_Email(
                getBoardPkOrThrow(boardId), email)
            .orElseThrow(() -> new NotLikedError("좋아요하지 않은 게시글입니다."));

        likeRepository.delete(boardLike);

        Board board = boardLike.getBoard();
        if (board.getLikeCount() > 0) {
            board.setLikeCount(board.getLikeCount() - 1);
        }

        return ResponseToggleLike.builder()
            .isLiked(false)
            .likeCount(board.getLikeCount())
            .build();
    }
}