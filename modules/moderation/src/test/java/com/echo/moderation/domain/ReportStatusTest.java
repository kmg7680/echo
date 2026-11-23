package com.echo.moderation.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReportStatusTest {

    @Test
    @DisplayName("PENDING→REVIEWED, REVIEWED→RESOLVED만 허용")
    void forward_only() {
        assertTrue(ReportStatus.PENDING.canTransitionTo(ReportStatus.REVIEWED));
        assertTrue(ReportStatus.REVIEWED.canTransitionTo(ReportStatus.RESOLVED));
    }

    @Test
    @DisplayName("건너뛰기·역방향·동일 상태·RESOLVED 이후 전환은 불허")
    void others_rejected() {
        assertFalse(ReportStatus.PENDING.canTransitionTo(ReportStatus.RESOLVED));
        assertFalse(ReportStatus.REVIEWED.canTransitionTo(ReportStatus.PENDING));
        assertFalse(ReportStatus.PENDING.canTransitionTo(ReportStatus.PENDING));
        assertFalse(ReportStatus.RESOLVED.canTransitionTo(ReportStatus.REVIEWED));
    }
}
