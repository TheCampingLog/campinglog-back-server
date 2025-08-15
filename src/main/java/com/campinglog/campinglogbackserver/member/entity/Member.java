package com.campinglog.campinglogbackserver.member.entity;

import com.campinglog.campinglogbackserver.board.entity.Board;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "members")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

  @Id
  @Column(name = "email")
  String email;

  @Column(name = "password", nullable = false)
  String password;
  @Column(name = "name", nullable = false)
  String name;
  @Column(name = "nickname", nullable = false)
  String nickname;
  @Column(name = "birthday", nullable = false)
  LocalDate birthday;
  @Column(name = "phone_number", nullable = false)
  String phoneNumber;
  @Column(name = "profile_image")
  String profileImage;
  @Column(name = "role", nullable = false)
  @Enumerated(EnumType.STRING)
  Role role;
  @Column(name = "member_grade", nullable = false)
  @Enumerated(EnumType.STRING)
  MemberGrade memberGrade;
  @Column(name = "join_date", nullable = false)
  LocalDate joinDate;

  @PrePersist
  public void prePersist() {

    if (this.role == null) {
      this.role = Role.USER;
    }

    if (this.memberGrade == null) {
      this.memberGrade = MemberGrade.GREEN;
    }

    if (this.joinDate == null) {
      this.joinDate = LocalDate.now();
    }

  }

  public enum Role {
    USER,
    ADMIN
  }

  public enum MemberGrade {
    GREEN, BLUE, RED, BLACK
  }
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Board> boards = new ArrayList<>();

}
