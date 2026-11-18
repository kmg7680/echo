package com.echo.api.timeline;

import com.echo.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 타임라인 HTTP API 컨트롤러.
 */
@RestController
@RequestMapping("/timeline")
@Tag(name = "Timeline", description = "타임라인 API")
public class TimelineController {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final TimelineAssembler assembler;

    public TimelineController(TimelineAssembler assembler) {
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(summary = "타임라인 조회", description = "최신 글부터 limit개(기본 20, 최대 100)를 조회합니다.")
    public ApiResponse<List<TimelineEntryResponse>> getTimeline(
            @RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit) {

        int bounded = Math.max(1, Math.min(limit, MAX_LIMIT));
        List<TimelineEntryResponse> entries = assembler.assemble(bounded).stream()
                .map(TimelineEntryResponse::from)
                .toList();
        return ApiResponse.ok(entries);
    }
}
