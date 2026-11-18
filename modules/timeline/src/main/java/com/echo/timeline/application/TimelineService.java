package com.echo.timeline.application;

import com.echo.timeline.domain.TimelineEntry;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 전달받은 항목을 최신순 피드로 조립한다.
 * 글 데이터 조회는 앱 계층이 맡고, 이 서비스는 정렬·제한만 담당한다.
 */
@Service
public class TimelineService {

    private static final Comparator<TimelineEntry> LATEST_FIRST =
            Comparator.comparing(TimelineEntry::createdAt, Comparator.reverseOrder())
                    .thenComparing(TimelineEntry::postId, Comparator.reverseOrder());

    public List<TimelineEntry> assemble(List<TimelineEntry> entries, int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit은 1 이상이어야 합니다. limit=" + limit);
        }
        return entries.stream()
                .sorted(LATEST_FIRST)
                .limit(limit)
                .toList();
    }
}
