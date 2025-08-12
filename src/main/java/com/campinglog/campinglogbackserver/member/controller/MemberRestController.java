package com.campinglog.campinglogbackserver.member.controller;

import com.campinglog.campinglogbackserver.member.dto.request.RequestAddMember;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMember;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberBoardList;
import com.campinglog.campinglogbackserver.member.entity.Member;
import com.campinglog.campinglogbackserver.member.service.MemberService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberRestController {

  private final MemberService memberService;

  @PostMapping
  public ResponseEntity<Map<String, String>> addMember(
      @Valid @RequestBody RequestAddMember requestAddMember) {
    memberService.addMember(requestAddMember);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping("/mypage")
  public ResponseEntity<ResponseGetMember> getMember(
          @AuthenticationPrincipal String email) {
    return ResponseEntity.ok(memberService.getMemberByEmail(email));
  }

  @GetMapping("/mypage/boards")
  public ResponseEntity<ResponseGetMemberBoardList> getMyBoards(
          @AuthenticationPrincipal String email,
          @RequestParam(name = "pageNo", defaultValue = "1") int pageNo
  ) {
    return ResponseEntity.ok(memberService.getMyBoards(email, pageNo));
  }


  @GetMapping("/test")
  public ResponseEntity<Map<String, String>> test(
      @AuthenticationPrincipal String email) {
    return ResponseEntity.ok(Map.of("email", email));
  }

}
