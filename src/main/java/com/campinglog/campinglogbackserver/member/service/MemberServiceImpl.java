package com.campinglog.campinglogbackserver.member.service;

import com.campinglog.campinglogbackserver.board.entity.Board;
import com.campinglog.campinglogbackserver.board.repository.BoardRepository;
import com.campinglog.campinglogbackserver.member.dto.request.RequestAddMember;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMember;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberBoard;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberBoardList;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.exception.MemberCreationError;
import com.campinglog.campinglogbackserver.member.exception.MemberNotFoundError;
import com.campinglog.campinglogbackserver.member.repository.MemberRespository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

  private final MemberRespository memberRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final ModelMapper modelMapper;                    // 주입받아 재사용
  private static final int PAGE_SIZE = 4;
  private final BoardRepository boardRepository; //@OneToMany 사용하지 않고

  @Override
  public void addMember(RequestAddMember requestAddMember) {
    Member member = new ModelMapper().map(requestAddMember, Member.class);
    String encodedPassword = bCryptPasswordEncoder.encode(requestAddMember.getPassword());

    member.setPassword(encodedPassword);

    try {
      memberRepository.save(member);
    } catch (RuntimeException e) {
      throw new MemberCreationError("회원 가입에 실패했습니다");
    }
  }

  @Override
  @Transactional
  public ResponseGetMember getMemberByEmail(String email) {
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new MemberNotFoundError("해당 이메일로 회원을 찾을 수 없습니다. email=" + email));
    return modelMapper.map(member, ResponseGetMember.class);
  }

  @Override
  public ResponseGetMemberBoardList getMyBoards(String email, int pageNo) {
    int pageIndex = Math.max(pageNo - 1, 0); // 1-based → 0-based
    PageRequest pageable = PageRequest.of(pageIndex, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));

    Page<Board> page = boardRepository.findByEmail(email, pageable);

    List<ResponseGetMemberBoard> items = page.getContent().stream()
            .map(board -> modelMapper.map(board, ResponseGetMemberBoard.class))
            .collect(Collectors.toList());

    return ResponseGetMemberBoardList.builder()
            .items(items)
            .page(pageNo)
            .size(PAGE_SIZE)
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .build();
  }
}
