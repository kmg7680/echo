---
name: known-limitations
description: Echo 보안 검토에서 합의된 알려진 한계(인증 부재, X-User-Id 위조, 기본값 1) — 매번 새 발견으로 부풀리지 말고 Info로
metadata:
  type: project
---

- 인증 미도입(코드 주석상 "15장에서 도입"). 모든 컨트롤러가 `X-User-Id` 헤더로 사용자 식별 → 위조 가능.
- `@RequestHeader(value = "X-User-Id", defaultValue = "1")` 패턴: 헤더 누락 시 user 1로 동작. 사용자 존재 검증도 없음.
- 역할(관리자/모더레이터) 개념 자체가 없음 → 신고 상태 변경(PATCH /reports/{id}/status)·신고 조회(GET /reports/{id}) 무인가.

**Why:** 학습용 샘플이라 인증은 후속 챕터 범위. 2026-11-23 moderation 검토에서 확인.
**How to apply:** 헤더 위조 자체는 Info로 보고. 단, 역할 검사가 필요한 관리성 엔드포인트(신고 처리 등)는 "인증 도입 시 반드시 막을 지점"으로 High 유지. 인증이 도입되면 이 메모를 갱신할 것. 관련: [[security-conventions]]
