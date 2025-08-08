package com.campinglog.campinglogbackserver.account.controller;

import com.campinglog.campinglogbackserver.account.dto.request.RequestAddMember;
import com.campinglog.campinglogbackserver.account.service.MemberService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class MemberRestController {

  private final MemberService memberService;

  @PostMapping
  public ResponseEntity<Map<String, String>> addUser(
      @Valid @RequestBody RequestAddMember requestAddMember) {
    memberService.addMember(requestAddMember);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping("/test")
  public ResponseEntity<Map<String, String>> test(
      @AuthenticationPrincipal String email) {
    return ResponseEntity.ok(Map.of("email", email));
  }


}
