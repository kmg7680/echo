---
name: scaffold-module
description: Echo에 새 도메인 모듈(modules/{name})의 헥사고날 골격(domain/application/infrastructure), 짝 테스트 자리, customer-api Controller, 등록 설정까지 잡아준다. 모듈 이름을 인자로 받는다. 새 모듈 신설 요청 시 사용.
argument-hint: <module-name> [Entity]
disable-model-invocation: true
---

# /scaffold-module

`$ARGUMENTS` = 모듈 이름(소문자, 예: `bookmark`) + 선택적 엔티티명(기본: 모듈명의 PascalCase).
이하 `{name}` = 모듈명, `{Name}` = 엔티티명(PascalCase). 기준 구현은 `modules/follow`.

## 시작 전
1. 인자가 없으면 모듈 이름을 묻는다.
2. `modules/{name}`이 이미 있으면 중단하고 알린다(덮어쓰지 않는다).
3. 챕터에서 명시하지 않은 모듈이면 신설해도 되는지 사용자에게 확인한다(`.claude/rules/architecture.md`).
4. 다른 모듈 데이터가 필요한 기능이면 모듈끼리 잇지 말고 Controller에서 조립한다고 안내한다.

## 만들 것 (이 순서)
각 파일의 코드 템플릿은 참조 파일에 있다. 읽고 플레이스홀더만 치환한다.

| # | 대상 | 템플릿 |
|---|---|---|
| 1 | `settings.gradle.kts`에 `include("modules:{name}")` | [references/registration.md](references/registration.md) |
| 2 | `modules/{name}/build.gradle.kts` | [references/build-gradle.md](references/build-gradle.md) |
| 3 | domain: `{Name}`, 도메인 예외 | [references/hexagonal-layers.md](references/hexagonal-layers.md) |
| 4 | infrastructure: `{Name}Repository` | 〃 |
| 5 | application: `{Name}Service` | 〃 |
| 6 | `EchoApplication.java` 스캔 3곳 + `apps/customer-api/build.gradle.kts` 의존 | [references/registration.md](references/registration.md) |
| 7 | customer-api: Controller, 요청/응답 record | [references/api-layer.md](references/api-layer.md) |
| 8 | 짝 테스트 3종 | [references/tests.md](references/tests.md) |

디렉터리:
```
modules/{name}/src/main/java/com/echo/{name}/{domain,infrastructure,application}/
modules/{name}/src/test/java/com/echo/{name}/{domain,application}/
apps/customer-api/src/main/java/com/echo/api/{name}/
apps/customer-api/src/test/java/com/echo/api/{Name}IntegrationTest.java
```

골격만 잡는 것이 목적이다. 도메인 필드·불변식·엔드포인트의 실제 내용은 사용자에게 확인하거나 TODO로 남긴다. 추측으로 채우지 않는다.

## echo 관습 (항상 지킨다)
- **도메인 모듈에 Spring Web 금지** — build.gradle.kts에 web 스타터 없음, `org.springframework.web`/`http` import 없음. POJO + JPA만.
- **모듈 간 직접 의존 금지** — 모듈의 build.gradle.kts에는 `project(":common")`만. `project(":modules:*")`는 `apps/*`에서만. 모듈 간 공유가 필요하면 `common` 경유.
- **의존 방향** — apps → modules, common / modules → common.
- **Controller·HTTP DTO는 `apps/customer-api`** — 모듈 안에 두지 않는다.
- **응답은 `ApiResponse.ok(data, "메시지")`** 로 감싼다. 반환 타입은 `ApiResponse<...>`.
- **DTO는 record.**
- **패키지** — `com.echo.{module}.{layer}`.
- **트랜잭션** — 서비스 클래스 `@Transactional(readOnly = true)`, 쓰기 메서드만 `@Transactional`.
- **예외 매핑** — domain/application은 던지기만, HTTP 상태(404/409/400) 변환은 Controller에서.
- **짝 테스트** — domain, application, 통합 테스트를 각각 둔다.
- **settings.gradle.kts 등록 필수.**

## 마무리 검증
1. `./gradlew :modules:{name}:test`
2. `./gradlew :apps:customer-api:test --tests "com.echo.api.{Name}IntegrationTest"`
3. `./gradlew build`
4. 점검: 모듈 build.gradle.kts에 web 스타터·`:modules:*` 의존이 없는지 grep으로 확인.

실패하면 출력을 그대로 보고하고, 통과했다고 단정하지 않는다. 커밋은 사용자가 요청할 때만 한다.
