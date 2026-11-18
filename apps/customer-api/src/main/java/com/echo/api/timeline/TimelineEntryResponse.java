package com.echo.api.timeline;

import com.echo.timeline.domain.TimelineEntry;
import java.time.LocalDateTime;

/**
 * GET /timeline 응답 DTO.
 */
public record TimelineEntryResponse(Long postId, Long authorId, String content, LocalDateTime createdAt) {

    public static TimelineEntryResponse from(TimelineEntry entry) {
        return new TimelineEntryResponse(
            entry.postId(),
            entry.authorId(),
            entry.content(),
            entry.createdAt()
        );
    }
}
