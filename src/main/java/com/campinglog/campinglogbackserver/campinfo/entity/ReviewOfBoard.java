package com.campinglog.campinglogbackserver.campinfo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "review_of_board")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewOfBoard {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long Id;

  @Column(name = "review_count", nullable = false)
  private Integer reviewCount;

  @Column(name = "review_average", nullable = false)
  private Double reviewAverage;

  @Column(name = "map_x", nullable = false, unique = true)
  private String mapX;

  @Column(name = "map_y", nullable = false, unique = true)
  private String mapY;

}
