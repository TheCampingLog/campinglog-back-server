package com.campinglog.campinglogbackserver.board.service;

import com.campinglog.campinglogbackserver.board.dto.request.RequestAddBoard;
import com.campinglog.campinglogbackserver.board.dto.request.RequestSetBoard;

public interface BoardService {

    void addBoard(RequestAddBoard requestAddBoard);

    void setBoard(RequestSetBoard requestSetBoard);

    void deleteBoard(String boardId);
}
