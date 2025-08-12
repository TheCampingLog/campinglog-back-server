package com.campinglog.campinglogbackserver.member.controller;

import com.campinglog.campinglogbackserver.member.dto.request.*;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMember;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberBoardList;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberProfileImage;
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

  @PutMapping("/mypage")
  public ResponseEntity<Map<String, String>> updateMember(
          @AuthenticationPrincipal String email,
          @Valid @RequestBody RequestUpdateMember request
  ) {
    memberService.updateMember(email, request);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/mypage/boards")
  public ResponseEntity<ResponseGetMemberBoardList> getMyBoards(
          @AuthenticationPrincipal String email,
          @RequestParam(name = "pageNo", defaultValue = "1") int pageNo
  ) {
    return ResponseEntity.ok(memberService.getMyBoards(email, pageNo));
  }

  @GetMapping("/mypage/profileImage")
  public ResponseEntity<ResponseGetMemberProfileImage> getProfileImage(
          @AuthenticationPrincipal String email
  ) {
    return ResponseEntity.ok(memberService.getProfileImage(email));
  }

  @PostMapping("/mypage/profileImage")
  public ResponseEntity<Map<String, String>> registerProfileImage(
          @AuthenticationPrincipal String email,
          @Valid @RequestBody RequestSetProfileImage request
  ) {
    memberService.addProfileImage(email, request);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PutMapping("/mypage/profileImage")
  public ResponseEntity<Map<String, String>> updateProfileImage(
          @AuthenticationPrincipal String email,
          @Valid @RequestBody RequestSetProfileImage request
  ) {
    memberService.updateProfileImage(email, request);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PostMapping("/mypage/verifyPassword")
  public ResponseEntity<Map<String, String>> verifyPassword(
          @AuthenticationPrincipal String email,
          @Valid @RequestBody RequestVerifyPassword request
  ) {
    memberService.verifyPassword(email, request);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/check/email")
  public ResponseEntity<Map<String, String>> checkEmail(@RequestParam String email) {
    memberService.assertEmailAvailable(email);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/check/nickname")
  public ResponseEntity<Map<String, String>> checkNickname(@RequestParam String nickname) {
    memberService.assertNicknameAvailable(nickname);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PutMapping("/mypage/password")
  public ResponseEntity<Map<String, String>> changePassword(
          @AuthenticationPrincipal String email,
          @Valid @RequestBody RequestChangePassword request
  ) {
    memberService.changePassword(email, request);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/test")
  public ResponseEntity<Map<String, String>> test(
      @AuthenticationPrincipal String email) {
    return ResponseEntity.ok(Map.of("email", email));
  }


}
