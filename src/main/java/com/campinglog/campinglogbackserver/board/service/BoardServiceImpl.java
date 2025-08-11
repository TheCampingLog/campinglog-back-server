package com.campinglog.campinglogbackserver.board.service;

import com.campinglog.campinglogbackserver.board.dto.request.RequestAddBoard;
import com.campinglog.campinglogbackserver.board.entity.Board;
import com.campinglog.campinglogbackserver.board.repository.BoardRepositry;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepositry boardRepositry;
    private final ModelMapper modelMapper;

    @Override
    public void addBoard(RequestAddBoard requestAddBoard) {
        Board board = modelMapper.map(requestAddBoard, Board.class);
        Board savedBoard = boardRepositry.save(board);
    }
}
