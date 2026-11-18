package com.echo.timeline.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.echo.timeline.domain.TimelineEntry;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimelineServiceTest {

    private final TimelineService service = new TimelineService();
    private final LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);

    private TimelineEntry entry(long id, LocalDateTime createdAt) {
        return new TimelineEntry(id, 1L, "글" + id, createdAt);
    }

    @Test
    @DisplayName("최신순으로 정렬한다")
    void sortsLatestFirst() {
        var result = service.assemble(List.of(
                entry(1, now), entry(2, now.plusMinutes(2)), entry(3, now.plusMinutes(1))), 10);

        assertEquals(List.of(2L, 3L, 1L), result.stream().map(TimelineEntry::postId).toList());
    }

    @Test
    @DisplayName("createdAt이 같으면 postId 내림차순")
    void tieBreaksByPostIdDesc() {
        var result = service.assemble(List.of(entry(1, now), entry(3, now), entry(2, now)), 10);

        assertEquals(List.of(3L, 2L, 1L), result.stream().map(TimelineEntry::postId).toList());
    }

    @Test
    @DisplayName("limit만큼만 반환한다")
    void appliesLimit() {
        var result = service.assemble(List.of(
                entry(1, now), entry(2, now.plusMinutes(1)), entry(3, now.plusMinutes(2))), 2);

        assertEquals(List.of(3L, 2L), result.stream().map(TimelineEntry::postId).toList());
    }

    @Test
    @DisplayName("빈 입력은 빈 결과")
    void emptyInput() {
        assertTrue(service.assemble(List.of(), 5).isEmpty());
    }

    @Test
    @DisplayName("limit이 1 미만이면 예외")
    void rejectsInvalidLimit() {
        assertThrows(IllegalArgumentException.class, () -> service.assemble(List.of(), 0));
    }
}
