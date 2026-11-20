#!/usr/bin/env bash
# PostToolUse 훅 — Java 파일이 Edit/Write/MultiEdit로 수정되면, 그 파일이 속한
# 모듈의 테스트만 실행한다.
#  - 테스트 실패: 결과를 stderr로 내보내고 exit 2 (Claude에게 피드백되어 자동 수정 유도)
#  - Write로 만든 modules/*/src/main 클래스에 짝 테스트(<이름>Test.java)가 없으면 경고
#    (exit 0 + additionalContext JSON — 막지는 않고 Claude/사용자에게만 알린다)
#  - 그 외 성공은 아무것도 출력하지 않는다 (조용히 통과)
set -uo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

# 1) 훅 입력(JSON, stdin)에서 도구 이름과 수정된 파일 경로 추출
input="$(cat)"
tool_name="$(printf '%s' "$input" | jq -r '.tool_name // empty')"
file_path="$(printf '%s' "$input" | jq -r '.tool_input.file_path // empty')"

# 경로가 없으면(해당 없음) 조용히 통과
[ -z "$file_path" ] && exit 0

# 2) Java 파일이 아니면 통과 (build.gradle.kts, 설정 파일 등은 무시)
case "$file_path" in
  *.java) ;;
  *) exit 0 ;;
esac

# 3) 파일 경로로 모듈(Gradle 테스트 태스크)을 판별
case "$file_path" in
  */modules/post/*)      task=":modules:post:test";     module_dir="modules/post" ;;
  */modules/timeline/*)  task=":modules:timeline:test"; module_dir="modules/timeline" ;;
  */modules/follow/*)    task=":modules:follow:test";   module_dir="modules/follow" ;;
  */modules/user/*)      task=":modules:user:test";     module_dir="modules/user" ;;
  */apps/customer-api/*) task=":apps:customer-api:test"; module_dir="" ;;
  *) exit 0 ;;  # 매핑되는 모듈 없음 → 통과
esac

# 4) 짝 테스트 확인 — Write로 만든 modules/*/src/main 의 클래스만 대상
#    (인터페이스·예외·package-info·테스트 파일은 제외. apps는 통합 테스트를 쓰므로 제외)
missing_test_warning=""
if [ "$tool_name" = "Write" ] && [ -n "$module_dir" ]; then
  case "$file_path" in
    */src/main/java/*)
      base="$(basename "$file_path" .java)"
      case "$base" in
        package-info|*Exception) ;;  # 짝 테스트 불필요
        *)
          if ! grep -qE '^[[:space:]]*(public[[:space:]]+)?(sealed[[:space:]]+)?interface[[:space:]]' "$file_path" 2>/dev/null; then
            test_root="${file_path%%/src/main/java/*}/src/test/java"
            if [ -z "$(find "$test_root" \( -name "${base}Test.java" -o -name "${base}Tests.java" \) 2>/dev/null | head -n1)" ]; then
              missing_test_warning="⚠️ ${base} 에 짝 테스트(${base}Test.java)가 없습니다. ${test_root#"$PROJECT_ROOT"/} 아래에 테스트를 추가해줘."
            fi
          fi
          ;;
      esac
      ;;
  esac
fi

# 5) 프로젝트 루트에서 해당 모듈 테스트만 실행
cd "$PROJECT_ROOT" || exit 0

output="$(./gradlew "$task" 2>&1)"
status=$?

if [ "$status" -ne 0 ]; then
  {
    printf '%s\n\n' "$output"
    printf '❌ %s 실패 — 위 테스트 오류를 확인하고 코드를 고쳐줘.\n' "$task"
    [ -n "$missing_test_warning" ] && printf '\n%s\n' "$missing_test_warning"
  } >&2
  exit 2
fi

# 6) 테스트 통과 — 경고가 있을 때만 출력, 없으면 침묵
if [ -n "$missing_test_warning" ]; then
  jq -n --arg msg "$missing_test_warning" '{
    systemMessage: $msg,
    hookSpecificOutput: { hookEventName: "PostToolUse", additionalContext: $msg }
  }'
fi
exit 0
