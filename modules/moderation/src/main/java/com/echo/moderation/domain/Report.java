package com.echo.moderation.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 게시물 신고 엔티티. domain 계층이므로 Spring 의존 없이 POJO + JPA만 사용한다.
 *
 * <p>접수 시 상태는 항상 PENDING이다. 불변식 위반은 {@link IllegalArgumentException},
 * 허용되지 않는 상태 전환은 {@link ReportException}을 던지고 HTTP 상태 변환은 컨트롤러가 맡는다.
 */
@Entity
@Table(name = "reports")
public class Report {

    public static final int MAX_REASON_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reporterId;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false, length = MAX_REASON_LENGTH)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Report() {
    }

    public Report(Long reporterId, Long postId, String reason) {
        if (reporterId == null) {
            throw new IllegalArgumentException("reporterId는 필수입니다.");
        }
        if (postId == null) {
            throw new IllegalArgumentException("postId는 필수입니다.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("신고 사유는 비어 있을 수 없습니다.");
        }
        if (reason.length() > MAX_REASON_LENGTH) {
            throw new IllegalArgumentException("신고 사유는 " + MAX_REASON_LENGTH + "자를 초과할 수 없습니다.");
        }
        this.reporterId = reporterId;
        this.postId = postId;
        this.reason = reason;
        this.status = ReportStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /** 상태 전환. PENDING→REVIEWED→RESOLVED 외에는 {@link ReportException}. */
    public void changeStatus(ReportStatus target) {
        if (target == null) {
            throw new IllegalArgumentException("status는 필수입니다.");
        }
        if (!status.canTransitionTo(target)) {
            throw new ReportException("허용되지 않는 상태 전환입니다. " + status + " → " + target);
        }
        this.status = target;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getReporterId() { return reporterId; }
    public Long getPostId() { return postId; }
    public String getReason() { return reason; }
    public ReportStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
