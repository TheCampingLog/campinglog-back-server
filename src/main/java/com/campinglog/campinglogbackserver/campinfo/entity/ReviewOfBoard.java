package com.campinglog.campinglogbackserver.campinfo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "review_of_board",
    uniqueConstraints = @UniqueConstraint(name = "uk_review_of_board_mapx_mapy",
    columnNames = {"map_x", "map_y"}))
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

  @Column(name = "map_x", nullable = false)
  private String mapX;

  @Column(name = "map_y", nullable = false)
  private String mapY;

}
