package com.campinglog.campinglogbackserver.board.entity;

import com.campinglog.campinglogbackserver.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter //@Data 사용시 문제생길수있음
@Setter
@Table(name = "comment")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"board", "member"})  // 순환참조 방지
@EqualsAndHashCode(exclude = {"board", "member"})  // 순환참조 방지
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment_id", nullable = false, unique = true)
    private String commentId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    @ColumnDefault(value = "CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", referencedColumnName = "id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", referencedColumnName = "email", nullable = false)
    private Member member;

    // 편의 메서드
    public String getBoardId() {
        return board != null ? board.getBoardId() : null;
    }

    public String getNickname() {
        return member != null ? member.getNickname() : null;
    }

    public String getEmail() {
        return member != null ? member.getEmail() : null;
    }

    @PrePersist
    private void prePersist() {
        if (this.commentId == null || this.commentId.isBlank()) {
            this.commentId = java.util.UUID.randomUUID().toString();
        }
    }
}