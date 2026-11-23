package com.echo.moderation.domain;

/**
 * 신고 도메인 규칙 위반(허용되지 않는 상태 전환 등) 시 발생하는 예외.
 *
 * <p>HTTP 상태 변환(409)은 컨트롤러(조립 지점)의 몫이다 — domain은 Spring Web에 의존하지 않는다.
 */
public class ReportException extends RuntimeException {

    public ReportException(String message) {
        super(message);
    }
}
