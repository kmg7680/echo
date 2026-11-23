package com.echo.moderation.domain;

/**
 * 신고 처리 상태. PENDING → REVIEWED → RESOLVED 순방향으로만 전환할 수 있다.
 */
public enum ReportStatus {
    PENDING,
    REVIEWED,
    RESOLVED;

    /** 허용된 다음 상태인지 — 바로 다음 단계로만 전환 가능. */
    public boolean canTransitionTo(ReportStatus target) {
        return switch (this) {
            case PENDING -> target == REVIEWED;
            case REVIEWED -> target == RESOLVED;
            case RESOLVED -> false;
        };
    }
}
