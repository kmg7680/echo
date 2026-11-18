# 세션 요약

_마지막 갱신: 2026-11-18 16:37:18 · 세션: 0b1c4ba9-bce3-45da-b084-278aa9187be5_

## 이번 세션 요청

- post 글 작성이 지금 어떻게 구현돼 있는지 코드를 읽고 설명해줘. Controller → Service → Repository로 이어지는 흐름과, 이 프로젝트가 지키는 컨벤션(ApiResponse 래핑, DTO record, 계층별 책임, 도메인 모듈의 Spring Web 의존 금지)을 짚어줘. 아직 코드는 바꾸지 마.
- 빌드하고 테스트를 돌려서 다 통과하는지 확인해줘(./gradlew build). 그리고 방금 추가한 GET /posts, GET /posts/{id}에 대한 통합 테스트도 기존 PostIntegrationTest 스타일로 추가해줘 — 글을 만든 뒤 단건·목록이 조회되는지, 없는 id는 404인지.
- !  brew install openjdk@25
- 커밋하고 태그 달ㅇㅏ서 푸시해줘. 태그는 v4.2-baseline 태깅
- 방금 읽은 POST 패턴을 그대로 따라서 글 조회 기능을 추가해줘. - GET /posts/{id} : 단건 조회. 없는 글이면 404 - GET /posts : 최신 글부터 정렬한 목록 조회 규칙: .claude/rules의 계층 규칙을 지켜. 도메인/application 모듈은 Spring Web에 의존하면 안 되니까 조회 결과가 없을 때의 404 같은…
- 그러면 내가 너에게 요청한 태그 버전으로 소스코드를 돌리고 푸시해줘. 모든 작업이 다 되어잇는거 같으니

## 바뀐 파일

_바뀐 파일 없음_

> 다음 세션은 이 파일을 읽고 이어서 작업한다.
