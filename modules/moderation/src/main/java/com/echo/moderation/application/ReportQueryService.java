package com.echo.moderation.application;

import com.echo.moderation.domain.Report;
import com.echo.moderation.infrastructure.ReportRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 신고 조회 전용 서비스(읽기 측). 쓰기는 {@link ReportService}가 담당한다. */
@Service
@Transactional(readOnly = true)
public class ReportQueryService {

    private final ReportRepository reportRepository;

    public ReportQueryService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    /** 단건 조회. 존재 여부 판단(404)은 호출자(컨트롤러) 몫이라 Optional로 반환한다. */
    public Optional<Report> findById(Long reportId) {
        return reportRepository.findById(reportId);
    }
}
