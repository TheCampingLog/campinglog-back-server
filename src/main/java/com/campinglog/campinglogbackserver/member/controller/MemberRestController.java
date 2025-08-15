package com.campinglog.campinglogbackserver.member.controller;

import com.campinglog.campinglogbackserver.member.dto.request.*;
import com.campinglog.campinglogbackserver.member.dto.request.RequestAddMember;
import com.campinglog.campinglogbackserver.member.dto.request.RequestChangePassword;
import com.campinglog.campinglogbackserver.member.dto.request.RequestUpdateMember;
import com.campinglog.campinglogbackserver.member.dto.request.RequestVerifyPassword;
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
@RequestMapping("/api/members")
public class MemberRestController {

  private final MemberService memberService;

  @PostMapping
  public ResponseEntity<Map<String, String>> addMember(
      @Valid @RequestBody RequestAddMember requestAddMember) {
    memberService.addMember(requestAddMember);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @DeleteMapping
  public ResponseEntity<Map<String, String>> deleteMember(
          @AuthenticationPrincipal String email
  ) {
    memberService.deleteMember(email);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PutMapping("/grade")
  public ResponseEntity<Map<String, Integer>> setMemberGrade() {
    int changed = memberService.updateGradeWeekly();
    return ResponseEntity.ok(Map.of("changed", changed));
  }

  @GetMapping("/mypage")
  public ResponseEntity<ResponseGetMember> getMember(
          @AuthenticationPrincipal String email) {
    return ResponseEntity.ok(memberService.getMember(email));
  }

  @PutMapping("/mypage")
  public ResponseEntity<Map<String, String>> setMember(
          @AuthenticationPrincipal String email,
          @Valid @RequestBody RequestUpdateMember request
  ) {
    memberService.setMember(email, request);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/mypage/boards")
  public ResponseEntity<ResponseGetMemberBoardList> getBoards(
          @AuthenticationPrincipal String email,
          @RequestParam(name = "pageNo", defaultValue = "1") int pageNo
  ) {
    return ResponseEntity.ok(memberService.getBoards(email, pageNo));
  }

  @GetMapping("/mypage/profile-image")
  public ResponseEntity<ResponseGetMemberProfileImage> getProfileImage(
          @AuthenticationPrincipal String email
  ) {
    return ResponseEntity.ok(memberService.getProfileImage(email));
  }

  @PostMapping("/mypage/profile-image")
  public ResponseEntity<Map<String, String>> addProfileImage(
          @AuthenticationPrincipal String email,
          @Valid @RequestBody RequestSetProfileImage request
  ) {
    memberService.addProfileImage(email, request);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PutMapping("/mypage/profile-image")
  public ResponseEntity<Map<String, String>> setProfileImage(
          @AuthenticationPrincipal String email,
          @Valid @RequestBody RequestSetProfileImage request
  ) {
    memberService.setProfileImage(email, request);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PutMapping("/mypage/password")
  public ResponseEntity<Map<String, String>> setPassword(
          @AuthenticationPrincipal String email,
          @Valid @RequestBody RequestChangePassword request
  ) {
    memberService.setPassword(email, request);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PostMapping("/mypage/password/verify")
  public ResponseEntity<Map<String, String>> verifyPassword(
          @AuthenticationPrincipal String email,
          @Valid @RequestBody RequestVerifyPassword request
  ) {
    memberService.verifyPassword(email, request);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("/email-availability")
  public ResponseEntity<Map<String, String>> checkEmailAvailable(@RequestParam String email) {
    memberService.checkEmailAvailable(email);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("/nickname-availability")
  public ResponseEntity<Map<String, String>> checkNicknameAvailable(@RequestParam String nickname) {
    memberService.checkNicknameAvailable(nickname);
    return new ResponseEntity<>(HttpStatus.OK);
  }
  
  @GetMapping("/test")
  public ResponseEntity<Map<String, String>> test(
      @AuthenticationPrincipal String email) {
    return ResponseEntity.ok(Map.of("email", email));
  }

}
