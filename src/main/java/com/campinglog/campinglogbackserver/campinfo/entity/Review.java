package com.campinglog.campinglogbackserver.campinfo.entity;

import com.campinglog.campinglogbackserver.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.repository.Query;

@Entity
@Data
@Table(name = "reviews")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "map_x", nullable = false)
  private String mapX;

  @Column(name = "map_y", nullable = false)
  private String mapY;

  @Column(name = "review_content", nullable = false)
  private String reviewContent;

  @Column(name = "review_score", nullable = false)
  private Double reviewScore;

  @Column(name = "review_image")
  private String reviewImage;

  @CreationTimestamp
  @Column(name = "post_at", nullable = false)
  private LocalDateTime postAt;

  @UpdateTimestamp
  @Column(name = "set_at")
  private LocalDateTime setAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "email", referencedColumnName = "email", nullable = false)
  private Member member;
}
