package com.campinglog.campinglogbackserver.campinfo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "reviews")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long Id;

  @Column(name = "map_x", nullable = false)
  private String mapX;

  @Column(name = "map_y", nullable = false)
  private String mapY;

  @Column(name = "review_content", nullable = false)
  private String reviewContent;

  @Column(name = "review_score", nullable = false)
  private String reviewScore;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "review_image")
  private String reviewImage;

  @CreationTimestamp
  @Column(name = "post_at", nullable = false)
  private LocalDateTime postAt;

  @UpdateTimestamp
  @Column(name = "set_at")
  private LocalDateTime setAt;
}
