package com.campinglog.campinglogbackserver.board.service;

import com.campinglog.campinglogbackserver.board.dto.request.RequestAddBoard;
import com.campinglog.campinglogbackserver.board.dto.request.RequestAddComment;
import com.campinglog.campinglogbackserver.board.dto.request.RequestAddLike;
import com.campinglog.campinglogbackserver.board.dto.request.RequestSetBoard;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardByCategory;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardByKeyword;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardDetail;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetBoardRank;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetComments;
import com.campinglog.campinglogbackserver.board.dto.response.ResponseGetLike;
import java.util.List;

public interface BoardService {

    void addBoard(RequestAddBoard requestAddBoard);

    void setBoard(RequestSetBoard requestSetBoard);

    void deleteBoard(String boardId);

    List<ResponseGetBoardRank> getBoardRank(int limit);

    ResponseGetBoardDetail getBoardDetail(String boardId);

    List<ResponseGetBoardByKeyword> searchBoards(String keyword, int page, int size);

    void addComment(String boardId, RequestAddComment requestAddComment);

    List<ResponseGetComments> getComments(String boardId, int page, int size);

    void addLike(String boardId, RequestAddLike requestAddLike);

    List<ResponseGetBoardByCategory> getBoardsByCategory(String category, int page, int size);

    ResponseGetLike getLikes(String boardId);

    void deleteLike(String boardId, String email);

}
