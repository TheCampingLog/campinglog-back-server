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
import java.util.List;

public interface BoardService {

    Board addBoard(RequestAddBoard requestAddBoard);

    void setBoard(RequestSetBoard requestSetBoard);

    void deleteBoard(String boardId);

    List<ResponseGetBoardRank> getBoardRank(int limit);

    ResponseGetBoardDetail getBoardDetail(String boardId, String email);

    ResponseGetBoardByKeywordWrapper searchBoards(String keyword, int page, int size);

    Comment addComment(String boardId, RequestAddComment requestAddComment);

    ResponseGetCommentsWrapper getComments(String boardId, int page, int size);

    ResponseToggleLike addLike(String boardId, RequestAddLike requestAddLike);

    ResponseGetBoardByCategoryWrapper getBoardsByCategory(String category, int page, int size);

    ResponseGetLike getLikes(String boardId);

    ResponseToggleLike deleteLike(String boardId, String email);

    void updateComment(String boardId, String commentId, RequestSetComment requestSetComment);

    void deleteComment(String boardId, String commentId);

}
