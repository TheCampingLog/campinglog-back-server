package com.campinglog.campinglogbackserver.board.service;

import com.campinglog.campinglogbackserver.board.dto.request.RequestAddBoard;
import com.campinglog.campinglogbackserver.board.dto.request.RequestSetBoard;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardByKeyword;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardRank;
import com.campinglog.campinglogbackserver.board.entity.Board;
import com.campinglog.campinglogbackserver.board.repository.BoardRepository;
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
    private final ModelMapper modelMapper;

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
}
