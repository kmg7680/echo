package com.echo.api.timeline;

import com.echo.post.application.PostService;
import com.echo.timeline.application.TimelineService;
import com.echo.timeline.domain.TimelineEntry;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * post → timeline 연결 지점.
 * 모듈 간 직접 의존을 피하기 위해, PostService 결과를 TimelineEntry로 변환해 넘기는 일은 앱 계층이 맡는다.
 */
@Component
public class TimelineAssembler {

    private final PostService postService;
    private final TimelineService timelineService;

    public TimelineAssembler(PostService postService, TimelineService timelineService) {
        this.postService = postService;
        this.timelineService = timelineService;
    }

    public List<TimelineEntry> assemble(int limit) {
        List<TimelineEntry> entries = postService.findLatest().stream()
                .map(post -> new TimelineEntry(
                        post.getId(), post.getAuthorId(), post.getContent(), post.getCreatedAt()))
                .toList();
        return timelineService.assemble(entries, limit);
    }
}
