package com.echo.moderation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.echo.moderation.domain.Report;
import com.echo.moderation.infrastructure.ReportRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportQueryServiceTest {

    @Mock
    ReportRepository reportRepository;

    @InjectMocks
    ReportQueryService reportQueryService;

    @Test
    @DisplayName("findById는 저장소 결과를 그대로 반환한다")
    void findById() {
        Report report = new Report(1L, 10L, "r");
        when(reportRepository.findById(5L)).thenReturn(Optional.of(report));
        when(reportRepository.findById(6L)).thenReturn(Optional.empty());

        assertEquals(report, reportQueryService.findById(5L).orElseThrow());
        assertTrue(reportQueryService.findById(6L).isEmpty());
    }
}
