package com.campinglog.campinglogbackserver.member.controller;

import com.campinglog.campinglogbackserver.campinfo.dto.response.ResponseGetMyReviewWrapper;
import com.campinglog.campinglogbackserver.campinfo.service.CampInfoService;
import com.campinglog.campinglogbackserver.member.dto.request.RequestAddMember;
import com.campinglog.campinglogbackserver.member.dto.request.RequestChangePassword;
import com.campinglog.campinglogbackserver.member.dto.request.RequestSetProfileImage;
import com.campinglog.campinglogbackserver.member.dto.request.RequestUpdateMember;
import com.campinglog.campinglogbackserver.member.dto.request.RequestVerifyPassword;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMember;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberActivity;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberBoardList;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberCommentList;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetMemberProfileImage;
import com.campinglog.campinglogbackserver.member.dto.response.ResponseGetToken;
import com.campinglog.campinglogbackserver.member.service.MemberService;
import com.campinglog.campinglogbackserver.member.service.RefreshTokenService;
import com.campinglog.campinglogbackserver.security.JwtProperties;
import com.campinglog.campinglogbackserver.security.RefreshProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Slf4j
public class MemberRestController {

  private final MemberService memberService;
  private final CampInfoService campInfoService;
  private final RefreshTokenService refreshTokenService;
  private final RefreshProperties refreshProperties;
  private final JwtProperties jwtProperties;

  @PostMapping
  public ResponseEntity<Map<String, String>> addMember(
      @Valid @RequestBody RequestAddMember requestAddMember) {
    memberService.addMember(requestAddMember);

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping
  public ResponseEntity<Map<String, String>> deleteMember(
      @AuthenticationPrincipal String email
  ) {
    memberService.deleteMember(email);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PutMapping("/grade")
  public ResponseEntity<Map<String, Integer>> setMemberGrade() {
    int changed = memberService.updateGradeWeekly();
    return ResponseEntity.ok(Map.of("changed", changed));
  }

  @GetMapping("/rank")
  public ResponseEntity<List<Map<String, Object>>> getWeeklyRanking(
      @RequestParam(defaultValue = "5") int limit
  ) {
    return ResponseEntity.ok(memberService.updateRankWeekly(limit));
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
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @GetMapping("/mypage/summary")
  public ResponseEntity<ResponseGetMemberActivity> getMySummary(
      @AuthenticationPrincipal String email) {
    ResponseGetMemberActivity summary = memberService.getMemberActivity(email);
    return ResponseEntity.ok(summary);
  }

  @GetMapping("/mypage/boards")
  public ResponseEntity<ResponseGetMemberBoardList> getBoards(
      @AuthenticationPrincipal String email,
      @RequestParam(name = "pageNo", defaultValue = "1") int pageNo
  ) {
    return ResponseEntity.ok(memberService.getBoards(email, pageNo));
  }

  @GetMapping("/mypage/comments")
  public ResponseEntity<ResponseGetMemberCommentList> getComments(
      @AuthenticationPrincipal String email,
      @RequestParam(name = "pageNo", defaultValue = "1") int pageNo
  ) {
    return ResponseEntity.ok(memberService.getComments(email, pageNo));
  }

  @GetMapping("/mypage/reviews")
  public ResponseEntity<Mono<ResponseGetMyReviewWrapper>> getReviews(
      @AuthenticationPrincipal String email,
      @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
      @RequestParam(name = "size", defaultValue = "4") int size) {
    return ResponseEntity.ok(campInfoService.getMyReviews(email, pageNo, size));
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
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PutMapping("/mypage/profile-image")
  public ResponseEntity<Map<String, String>> setProfileImage(
      @AuthenticationPrincipal String email,
      @Valid @RequestBody RequestSetProfileImage request
  ) {
    memberService.setProfileImage(email, request);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @DeleteMapping("/mypage/profile-image")
  public ResponseEntity<Void> deleteProfileImage(
      @AuthenticationPrincipal String email
  ) {
    memberService.deleteProfileImage(email);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PutMapping("/mypage/password")
  public ResponseEntity<Map<String, String>> setPassword(
      @AuthenticationPrincipal String email,
      @Valid @RequestBody RequestChangePassword request
  ) {
    memberService.setPassword(email, request);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PostMapping("/mypage/password/verify")
  public ResponseEntity<Map<String, String>> verifyPassword(
      @AuthenticationPrincipal String email,
      @Valid @RequestBody RequestVerifyPassword request
  ) {
    memberService.verifyPassword(email, request);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/email-availability")
  public ResponseEntity<Map<String, String>> checkEmailAvailable(@RequestParam String email) {
    memberService.checkEmailAvailable(email);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/nickname-availability")
  public ResponseEntity<Map<String, String>> checkNicknameAvailable(@RequestParam String nickname) {
    memberService.checkNicknameAvailable(nickname);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/test")
  public ResponseEntity<Map<String, String>> test(
      @AuthenticationPrincipal String email) {
    return ResponseEntity.ok(Map.of("email", email));
  }

  @PostMapping("/refresh")
  public ResponseEntity<Map<String, String>> checkRefreshToken(HttpServletRequest request,
      HttpServletResponse response) {

    log.info("roateRefreshToken 시작");
    ResponseGetToken newTokens = refreshTokenService.RotateRefreshToken(request);

    response.setHeader("Set-Cookie",
        String.format("%s=%s; Max-Age=%d; Path=%s; HttpOnly; SameSite=%s",
            refreshProperties.getCookie(),
            newTokens.getRefreshToken(), refreshProperties.getExpiration(), "/", "Strict"));

    response.addHeader(jwtProperties.getHeaderString(),
        jwtProperties.getTokenPrefix() + newTokens.getJwtToken());

    return ResponseEntity.ok(Map.of("message", "ok"));
  }

}
