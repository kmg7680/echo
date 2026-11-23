package com.echo.moderation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.echo.moderation.domain.Report;
import com.echo.moderation.domain.ReportException;
import com.echo.moderation.domain.ReportStatus;
import com.echo.moderation.infrastructure.ReportRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    ReportRepository reportRepository;

    @InjectMocks
    ReportService reportService;

    @Test
    @DisplayName("submit은 PENDING 신고를 저장해 반환한다")
    void submit_saves_pending() {
        when(reportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Report report = reportService.submit(1L, 10L, "스팸");

        assertEquals(ReportStatus.PENDING, report.getStatus());
        assertEquals(10L, report.getPostId());
    }

    @Test
    @DisplayName("changeStatus는 다음 단계로 전환한다")
    void changeStatus_ok() {
        Report report = new Report(1L, 10L, "r");
        when(reportRepository.findById(5L)).thenReturn(Optional.of(report));

        Optional<Report> result = reportService.changeStatus(5L, ReportStatus.REVIEWED);

        assertEquals(ReportStatus.REVIEWED, result.orElseThrow().getStatus());
    }

    @Test
    @DisplayName("없는 신고면 empty")
    void changeStatus_missing() {
        when(reportRepository.findById(5L)).thenReturn(Optional.empty());

        assertTrue(reportService.changeStatus(5L, ReportStatus.REVIEWED).isEmpty());
    }

    @Test
    @DisplayName("허용되지 않는 전환이면 ReportException")
    void changeStatus_invalid() {
        when(reportRepository.findById(5L)).thenReturn(Optional.of(new Report(1L, 10L, "r")));

        assertThrows(ReportException.class,
                () -> reportService.changeStatus(5L, ReportStatus.RESOLVED));
    }
}
