package com.echo.moderation.application;

import com.echo.moderation.domain.Report;
import com.echo.moderation.domain.ReportStatus;
import com.echo.moderation.infrastructure.ReportRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 신고 애플리케이션 서비스(쓰기 측). 읽기는 {@link ReportQueryService}가 담당한다.
 *
 * <p>대상 게시물 존재 여부(404)는 post 모듈이 필요하므로 이 서비스의 책임이 아니다 —
 * 모듈 간 직접 의존을 피하기 위해 조립 지점(컨트롤러)에서 검증한 뒤 호출한다.
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    /** 신고 접수. 항상 PENDING으로 저장된다. */
    @Transactional
    public Report submit(Long reporterId, Long postId, String reason) {
        return reportRepository.save(new Report(reporterId, postId, reason));
    }

    /** 상태 전환. 신고가 없으면 empty(404는 컨트롤러 몫), 허용되지 않는 전환이면 ReportException. */
    @Transactional
    public Optional<Report> changeStatus(Long reportId, ReportStatus target) {
        return reportRepository.findById(reportId).map(report -> {
            report.changeStatus(target);
            return report;
        });
    }
}
