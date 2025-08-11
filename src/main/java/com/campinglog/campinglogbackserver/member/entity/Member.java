package com.campinglog.campinglogbackserver.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
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

}
