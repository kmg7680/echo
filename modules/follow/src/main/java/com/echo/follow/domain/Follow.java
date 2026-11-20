package com.echo.follow.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 팔로우 관계. followerId가 followeeId를 팔로우한다.
 */
@Entity
@Table(
    name = "follows",
    uniqueConstraints = @UniqueConstraint(columnNames = {"followerId", "followeeId"})
)
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Long followerId;

    @Column(nullable = false, updatable = false)
    private Long followeeId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Follow() {
    }

    public Follow(Long followerId, Long followeeId) {
        if (followerId == null || followeeId == null) {
            throw new IllegalArgumentException("followerId, followeeId는 null일 수 없습니다.");
        }
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("자기 자신은 팔로우할 수 없습니다.");
        }
        this.followerId = followerId;
        this.followeeId = followeeId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getFollowerId() { return followerId; }
    public Long getFolloweeId() { return followeeId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
