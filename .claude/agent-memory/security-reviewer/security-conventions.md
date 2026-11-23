---
name: security-conventions
description: Echo 보안 관례·핵심 위치(금칙어 적용 지점, 시크릿 주입 방식, 오류 응답 처리)와 2026-11-23 기준 미해결 이슈 — 재검토 시 수정 여부 확인용
metadata:
  type: project
---

관례/위치
- 금칙어: DB `banned_words`, `BannedWordQueryService.findContainedWord`(매 요청 findAll + 소문자 contains). 적용은 조립 지점 `PostController.createPost`뿐. 새 텍스트 입력 경로가 생기면 적용 여부 반드시 확인.
- 시크릿: application.yml은 `${POSTGRES_PASSWORD}` 폴백 없음, docker-compose는 `:?` 필수화, `.env`는 gitignore. 양호.
- 전역 `@ControllerAdvice` 없음 → 오류는 ResponseStatusException + Spring 기본 오류 바디(ApiResponse 아님). `server.error.include-message` 미설정(기본 never)이라 reason은 외부 노출 안 됨 — 설정이 바뀌면 재검토.

2026-11-23 moderation 검토 시 미해결(다음 검토에서 수정 여부 확인)
- 금칙어 정규화 부재(NFKC·제로폭·공백/구두점·자모·homoglyph 우회), DB 직접 입력 대문자 금칙어는 영구 불일치.
- 신고 사유(reason)에 금칙어 미적용.
- reports: 중복 신고 UNIQUE 없음, 자기 글 신고 허용, FK/CHECK 없음, GET이 reporterId 노출(IDOR).
- `.mcp.json`에 DB 자격증명 평문(추적 파일).
- `.gitignore`에 `bin/` 없음(IDE 산출물 apps/customer-api/bin/ 커밋 위험).

**Why:** 반복 검토 시 같은 위치를 빠르게 재확인하기 위함.
**How to apply:** 위 항목은 파일을 다시 읽어 현재 상태 확인 후 해결됐으면 이 목록에서 제거. 관련: [[known-limitations]]
