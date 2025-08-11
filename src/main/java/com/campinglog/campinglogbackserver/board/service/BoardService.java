package com.campinglog.campinglogbackserver.board.service;

import com.campinglog.campinglogbackserver.board.dto.request.RequestAddBoard;
import com.campinglog.campinglogbackserver.board.dto.request.RequestSetBoard;

import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardDetail;

import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardByKeyword;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardRank;
import java.util.List;

public interface BoardService {

    void addBoard(RequestAddBoard requestAddBoard);

    void setBoard(RequestSetBoard requestSetBoard);

    void deleteBoard(String boardId);

    List<ResponseGetBoardRank> getBoardRank(int limit);

    ResponseGetBoardDetail getBoardDetail(String boardId);

    List<ResponseGetBoardByKeyword> searchBoards(String keyword, int page, int size);

}
