package com.campinglog.campinglogbackserver.board.entity;

import com.campinglog.campinglogbackserver.member.entity.Member;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
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
    @JoinColumn(name = "board_id", referencedColumnName = "board_id", nullable = false)
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
}