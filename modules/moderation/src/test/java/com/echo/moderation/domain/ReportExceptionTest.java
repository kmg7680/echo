package com.echo.moderation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReportExceptionTest {

    @Test
    @DisplayName("메시지를 보존한다")
    void keeps_message() {
        assertEquals("boom", new ReportException("boom").getMessage());
    }
}
