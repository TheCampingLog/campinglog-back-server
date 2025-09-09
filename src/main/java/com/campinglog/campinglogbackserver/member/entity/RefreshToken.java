package com.campinglog.campinglogbackserver.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String jti;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "email", referencedColumnName = "email", nullable = false)
  private Member member;

  @Column(nullable = false)
  private Integer used;

  @Column(nullable = false)
  private Date expiresAt;

  public boolean isUsed() {
    return this.used != null && this.used == 1;
  }

  public boolean isExpired() {
    return this.expiresAt != null && this.expiresAt.before(new Date());
  }

  public boolean isValid() {
    return !isUsed() && !isExpired();
  }

}
