package com.echo.timeline.domain;

import java.time.LocalDateTime;

/**
 * 타임라인에 노출되는 한 항목.
 * 다른 모듈(post)의 타입에 의존하지 않도록 timeline 자체 타입으로 둔다.
 */
public record TimelineEntry(Long postId, Long authorId, String content, LocalDateTime createdAt) {
}
