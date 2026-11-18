#!/usr/bin/env bash
# PostToolUse hook: Java 파일 수정 시 해당 모듈의 테스트만 실행한다.
# 실패하면 출력을 stderr로 내보내고 exit 2 → Claude에게 전달되어 수정하게 한다.
set -uo pipefail

input=$(cat)
file=$(jq -r '.tool_input.file_path // empty' <<<"$input")

[[ "$file" == *.java ]] || exit 0

root="${CLAUDE_PROJECT_DIR:-$(cd "$(dirname "$0")/../.." && pwd)}"
rel="${file#"$root"/}"

case "$rel" in
  modules/post/*)         task=":modules:post:test" ;;
  modules/timeline/*)     task=":modules:timeline:test" ;;
  modules/user/*)         task=":modules:user:test" ;;
  apps/customer-api/*)    task=":apps:customer-api:test" ;;
  *) exit 0 ;;
esac

cd "$root" || exit 0
if out=$(./gradlew "$task" --console=plain -q 2>&1); then
  exit 0
fi

{
  echo "테스트 실패 ($task) — $rel 수정 후 실행 결과:"
  echo "$out" | tail -60
} >&2
exit 2
