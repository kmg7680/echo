package com.echo.api.moderation;

import com.echo.moderation.domain.ReportStatus;
import jakarta.validation.constraints.NotNull;

/** PATCH /reports/{reportId}/status 요청 DTO. */
public record UpdateReportStatusRequest(
    @NotNull(message = "status는 필수입니다.")
    ReportStatus status
) {
}
