package com.echo.api.moderation;

import com.echo.common.ApiResponse;
import com.echo.moderation.application.ReportQueryService;
import com.echo.moderation.application.ReportService;
import com.echo.moderation.domain.Report;
import com.echo.moderation.domain.ReportException;
import com.echo.post.application.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * 신고 HTTP API 컨트롤러.
 *
 * <p>moderation과 post 두 모듈을 잇는 조립 지점(composition root)이다.
 * 신고 대상 게시물 존재 여부(404)는 post 모듈로 검증하고, 신고 자체는 moderation 모듈에 위임한다.
 * 두 모듈은 서로 직접 의존하지 않는다.
 *
 * <p>신고자는 X-User-Id 헤더로 식별한다(인증은 범위 외).
 */
@RestController
@RequestMapping("/reports")
@Tag(name = "Report", description = "신고 API")
public class ReportController {

    private final ReportService reportService;
    private final ReportQueryService reportQueryService;
    private final PostService postService;

    public ReportController(ReportService reportService,
                            ReportQueryService reportQueryService,
                            PostService postService) {
        this.reportService = reportService;
        this.reportQueryService = reportQueryService;
        this.postService = postService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "신고 접수", description = "X-User-Id가 게시물을 신고합니다. 접수 시 PENDING. 없는 게시물 404.")
    public ApiResponse<ReportResponse> submit(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @Valid @RequestBody CreateReportRequest request) {

        if (postService.findById(request.postId()).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "게시물을 찾을 수 없습니다. id=" + request.postId());
        }
        Report report = reportService.submit(userId, request.postId(), request.reason());
        return ApiResponse.ok(ReportResponse.from(report), "신고가 접수되었습니다.");
    }

    @PatchMapping("/{reportId}/status")
    @Operation(summary = "신고 상태 변경",
            description = "PENDING→REVIEWED→RESOLVED 순서로만 전환합니다. 없는 신고 404, 허용되지 않는 전환 409.")
    public ApiResponse<ReportResponse> changeStatus(
            @PathVariable Long reportId,
            @Valid @RequestBody UpdateReportStatusRequest request) {

        try {
            Report report = reportService.changeStatus(reportId, request.status())
                    .orElseThrow(() -> notFound(reportId));
            return ApiResponse.ok(ReportResponse.from(report), "신고 상태가 변경되었습니다.");
        } catch (ReportException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "신고 단건 조회", description = "id로 신고 하나를 조회합니다. 없으면 404.")
    public ApiResponse<ReportResponse> getReport(@PathVariable Long reportId) {
        Report report = reportQueryService.findById(reportId)
                .orElseThrow(() -> notFound(reportId));
        return ApiResponse.ok(ReportResponse.from(report));
    }

    private ResponseStatusException notFound(Long reportId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "신고를 찾을 수 없습니다. id=" + reportId);
    }
}
