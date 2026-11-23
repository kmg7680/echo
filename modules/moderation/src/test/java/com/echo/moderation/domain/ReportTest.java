package com.echo.moderation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReportTest {

    @Test
    @DisplayName("접수하면 PENDING 상태로 생성된다")
    void create_pending() {
        Report report = new Report(1L, 10L, "스팸입니다");

        assertEquals(ReportStatus.PENDING, report.getStatus());
        assertEquals(1L, report.getReporterId());
        assertEquals(10L, report.getPostId());
        assertNotNull(report.getCreatedAt());
    }

    @Test
    @DisplayName("신고자·게시물·사유가 없으면 예외")
    void create_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Report(null, 10L, "r"));
        assertThrows(IllegalArgumentException.class, () -> new Report(1L, null, "r"));
        assertThrows(IllegalArgumentException.class, () -> new Report(1L, 10L, " "));
        assertThrows(IllegalArgumentException.class, () -> new Report(1L, 10L, null));
    }

    @Test
    @DisplayName("사유가 500자를 넘으면 예외")
    void create_reason_too_long_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Report(1L, 10L, "a".repeat(Report.MAX_REASON_LENGTH + 1)));
    }

    @Test
    @DisplayName("PENDING→REVIEWED→RESOLVED 순서로 전환된다")
    void change_status_forward() {
        Report report = new Report(1L, 10L, "r");

        report.changeStatus(ReportStatus.REVIEWED);
        assertEquals(ReportStatus.REVIEWED, report.getStatus());

        report.changeStatus(ReportStatus.RESOLVED);
        assertEquals(ReportStatus.RESOLVED, report.getStatus());
    }

    @Test
    @DisplayName("단계를 건너뛰면 ReportException")
    void change_status_skip_throws() {
        Report report = new Report(1L, 10L, "r");

        assertThrows(ReportException.class, () -> report.changeStatus(ReportStatus.RESOLVED));
    }

    @Test
    @DisplayName("RESOLVED 이후에는 더 전환할 수 없다")
    void change_status_after_resolved_throws() {
        Report report = new Report(1L, 10L, "r");
        report.changeStatus(ReportStatus.REVIEWED);
        report.changeStatus(ReportStatus.RESOLVED);

        assertThrows(ReportException.class, () -> report.changeStatus(ReportStatus.REVIEWED));
    }

    @Test
    @DisplayName("전환 대상이 null이면 IllegalArgumentException")
    void change_status_null_throws() {
        Report report = new Report(1L, 10L, "r");

        assertThrows(IllegalArgumentException.class, () -> report.changeStatus(null));
    }
}
