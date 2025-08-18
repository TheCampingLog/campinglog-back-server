package com.campinglog.campinglogbackserver.board.entity;

import com.campinglog.campinglogbackserver.member.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter //@Data로 사용하면 문제생길수있음
@Setter
@Table(name = "board")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"member", "comments", "boardLikes"})  // 순환 참조 방지
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "board_id", nullable = false, unique = true)
    private String boardId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "board_image")
    private String boardImage;

    @Column(name = "view_count")
    @Builder.Default
    private int viewCount = 0;

    @Column(name = "like_count")
    @Builder.Default
    private int likeCount = 0;

    @Column(name = "comment_count")
    @Builder.Default
    private int commentCount = 0;

    @Column(name = "rank")
    private int rank;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    @ColumnDefault(value = "CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", referencedColumnName = "email", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BoardLike> boardLikes = new ArrayList<>();

    // 편의 메서드
    public String getEmail() {
        return member != null ? member.getEmail() : null;
    }

    public String getNickname() {
        return member != null ? member.getNickname() : null;
    }

    @PrePersist
    private void prePersist() {
        if (this.boardId == null || this.boardId.isBlank()) {
            this.boardId = java.util.UUID.randomUUID().toString();
        }
    }
}