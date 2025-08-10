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
@Table(name = "board_info")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardInfo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long Id;

  @Column(name = "board_name", nullable = false)
  private  String boardName;

  @Column(name = "review_count", nullable = false)
  private String reviewCount;

  @Column(name = "review_everage", nullable = false)
  private Double reviewEverage;

  @Column(name = "map_x", nullable = false, unique = true)
  private String mapX;

  @Column(name = "map_y", nullable = false, unique = true)
  private String mapY;

}
